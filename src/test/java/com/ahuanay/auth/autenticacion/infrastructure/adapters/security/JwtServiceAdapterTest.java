package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.rol.domain.model.Rol;
import com.ahuanay.auth.support.TestRoles;
import com.ahuanay.auth.support.TestUsuarios;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceAdapterTest {

    private static final String EMAIL = "user@email.com";
    private static final String SECRET = "clave-super-secreta-para-tests-de-unit-0123456789abcdef-0123456789";
    private static final String SECRET_ALTERNA = "otra-clave-distinta-para-tests-de-firma-0123456789abcdef-987654";

    private JwtServiceAdapter jwtServiceAdapter;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtServiceAdapter = crearAdaptador(SECRET, 900_000L, 604_800_000L);

        Rol rol = TestRoles.paciente(2L);
        usuario = TestUsuarios.base().conRol(rol).build();
    }

    private JwtServiceAdapter crearAdaptador(String secret, long jwtExpiration, long refreshExpiration) {
        JwtServiceAdapter adaptador = new JwtServiceAdapter();
        ReflectionTestUtils.setField(adaptador, "secretKey", secret);
        ReflectionTestUtils.setField(adaptador, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(adaptador, "refreshExpiration", refreshExpiration);
        ReflectionTestUtils.setField(adaptador, "zonaAplicacion", ZoneId.of("America/Lima"));
        return adaptador;
    }

    @Test
    @DisplayName("Debería firmar el access token con email como subject y los claims de roles y permisos cuando el usuario es válido")
    void deberiaFirmarAccessToken_CuandoElUsuarioEsValido() {
        // Act
        String accessToken = jwtServiceAdapter.generateAccessToken(usuario);

        // Assert
        assertThat(accessToken).isNotBlank();
        assertThat(jwtServiceAdapter.extractUsername(accessToken)).isEqualTo(EMAIL);
        Object userIdClaim = jwtServiceAdapter.extractClaim(accessToken, claims -> claims.get("userId"));
        assertThat(((Number) userIdClaim).intValue()).isEqualTo(1);

        Collection<String> rolesClaim = (Collection<String>) jwtServiceAdapter.extractClaim(accessToken, claims -> claims.get("roles"));
        assertThat(rolesClaim).containsExactlyInAnyOrder("PACIENTE");

        Collection<String> permisosClaim = (Collection<String>) jwtServiceAdapter.extractClaim(accessToken, claims -> claims.get("permisos"));
        assertThat(permisosClaim).containsExactlyInAnyOrder("PERMISO_READ");
    }

    @Test
    @DisplayName("Debería firmar el refresh token sin claims extra y con expiración posterior cuando se solicita la generación de tokens")
    void deberiaFirmarRefreshToken_CuandoSeSolicitaLaGeneracionDeTokens() {
        // Act
        String accessToken = jwtServiceAdapter.generateAccessToken(usuario);
        RefreshTokenGenerado refreshTokenGenerado = jwtServiceAdapter.generateRefreshToken(usuario);
        String refreshToken = refreshTokenGenerado.token();

        // Assert
        assertThat(refreshToken).isNotBlank();
        assertThat(refreshTokenGenerado.expiresAt()).isNotNull().isAfter(LocalDateTime.now());
        assertThat(jwtServiceAdapter.extractUsername(refreshToken)).isEqualTo(EMAIL);
        Object userIdClaim = jwtServiceAdapter.extractClaim(refreshToken, claims -> claims.get("userId"));
        Object rolesClaim = jwtServiceAdapter.extractClaim(refreshToken, claims -> claims.get("roles"));
        assertThat(userIdClaim).isNull();
        assertThat(rolesClaim).isNull();

        Date expiracionAccess = jwtServiceAdapter.extractClaim(accessToken, Claims::getExpiration);
        Date expiracionRefresh = jwtServiceAdapter.extractClaim(refreshToken, Claims::getExpiration);
        assertThat(expiracionRefresh).isAfter(expiracionAccess);
    }

    @Test
    @DisplayName("Debería validar el token cuando el username coincide y el token no ha expirado")
    void deberiaValidarToken_CuandoUsernameCoincideYNoHaExpirado() {
        // Arrange
        String accessToken = jwtServiceAdapter.generateAccessToken(usuario);

        // Act
        boolean valido = jwtServiceAdapter.isTokenValid(accessToken, EMAIL);

        // Assert
        assertThat(valido).isTrue();
    }

    @Test
    @DisplayName("Debería invalidar el token cuando el username no coincide con el subject del token")
    void deberiaInvalidarToken_CuandoElUsernameNoCoincide() {
        // Arrange
        String accessToken = jwtServiceAdapter.generateAccessToken(usuario);

        // Act
        boolean valido = jwtServiceAdapter.isTokenValid(accessToken, "otro@email.com");

        // Assert
        assertThat(valido).isFalse();
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token ya ha expirado")
    void deberiaLanzarCredencialInvalidoException_CuandoElTokenHaExpirado() {
        // Arrange
        JwtServiceAdapter adaptadorExpirado = crearAdaptador(SECRET, -1_000L, 604_800_000L);
        String tokenExpirado = adaptadorExpirado.generateAccessToken(usuario);

        // Act & Assert
        assertThatThrownBy(() -> jwtServiceAdapter.isTokenValid(tokenExpirado, EMAIL))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token es inválido o ha expirado.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token fue firmado con otra clave")
    void deberiaLanzarCredencialInvalidoException_CuandoElTokenFueFirmadoConOtraClave() {
        // Arrange
        JwtServiceAdapter otroServidor = crearAdaptador(SECRET_ALTERNA, 900_000L, 604_800_000L);
        String tokenAjeno = otroServidor.generateAccessToken(usuario);

        // Act & Assert
        assertThatThrownBy(() -> jwtServiceAdapter.extractUsername(tokenAjeno))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token es inválido o ha expirado.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token no tiene formato JWT válido")
    void deberiaLanzarCredencialInvalidoException_CuandoElTokenNoTieneFormatoJwt() {
        // Act & Assert
        assertThatThrownBy(() -> jwtServiceAdapter.extractUsername("no-es-un-jwt"))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token es inválido o ha expirado.");
    }
}