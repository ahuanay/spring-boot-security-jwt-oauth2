package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.port.out.JwtServicePort;
import com.ahuanay.auth.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.domain.model.Rol;
import com.ahuanay.auth.support.TestRoles;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginUseCaseImplTest {

    private static final String EMAIL = "useroauth2@gmail.com";
    private static final String PROVIDER_ID = "google-123";
    private static final String DEFAULT_ROLE = "PACIENTE";

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private JwtServicePort jwtServicePort;

    @InjectMocks
    private OAuth2LoginUseCaseImpl oAuth2LoginUseCase;

    private Rol rolDefecto;
    private Usuario usuarioExistente;
    private Usuario usuarioInactivo;
    private Usuario usuarioCreado;
    private RefreshTokenGenerado refreshTokenGenerado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oAuth2LoginUseCase, "defaultRoleName", DEFAULT_ROLE);

        rolDefecto = TestRoles.paciente(3L);
        usuarioExistente = TestUsuarios.base().google().conEmail(EMAIL).conProviderId(PROVIDER_ID).conRol(rolDefecto).build();
        usuarioInactivo = TestUsuarios.base().google().conEmail(EMAIL).conProviderId(PROVIDER_ID).conRol(rolDefecto).inactivo().build();
        usuarioCreado = TestUsuarios.base().google().conEmail(EMAIL).conProviderId(PROVIDER_ID).conRol(rolDefecto).build();
        refreshTokenGenerado = new RefreshTokenGenerado("refresh-token-456", LocalDateTime.now().plusDays(7));
    }

    @Test
    @DisplayName("Debería registrar el usuario con el rol por defecto y autenticarlo cuando es su primer login")
    void deberiaRegistrarYAutenticar_CuandoEsSuPrimerLogin() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(rolRepositoryPort.findByNombre(DEFAULT_ROLE)).thenReturn(Optional.of(rolDefecto));
        when(usuarioRepositoryPort.save(any(Usuario.class))).thenReturn(usuarioCreado);
        when(jwtServicePort.generateAccessToken(usuarioCreado)).thenReturn("access-token-123");
        when(jwtServicePort.generateRefreshToken(usuarioCreado)).thenReturn(refreshTokenGenerado);

        // Act
        AuthResponse response = oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, times(1)).findByNombre(DEFAULT_ROLE);
        verify(usuarioRepositoryPort, times(1)).save(any(Usuario.class));
        verify(jwtServicePort, times(1)).generateAccessToken(usuarioCreado);
        verify(jwtServicePort, times(1)).generateRefreshToken(usuarioCreado);
        verify(refreshTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería autenticar sin crear cuenta cuando el usuario ya existe")
    void deberiaAutenticar_CuandoElUsuarioYaExiste() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioExistente));
        when(jwtServicePort.generateAccessToken(usuarioExistente)).thenReturn("access-token-123");
        when(jwtServicePort.generateRefreshToken(usuarioExistente)).thenReturn(refreshTokenGenerado);

        // Act
        AuthResponse response = oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, never()).findByNombre(any());
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
        verify(jwtServicePort, times(1)).generateAccessToken(usuarioExistente);
        verify(jwtServicePort, times(1)).generateRefreshToken(usuarioExistente);
        verify(refreshTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando el usuario de OAuth2 se encuentra inactivo")
    void deberiaLanzarUsuarioInvalidoException_CuandoElUsuarioOAuth2SeEncuentraInactivo() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        assertThatThrownBy(() -> oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario se encuentra inactivo.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verifyNoInteractions(jwtServicePort, refreshTokenRepositoryPort);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando el rol por defecto no existe")
    void deberiaLanzarUsuarioInvalidoException_CuandoElRolDefectoNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(rolRepositoryPort.findByNombre(DEFAULT_ROLE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("Rol por defecto " + DEFAULT_ROLE + " no encontrado");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, times(1)).findByNombre(DEFAULT_ROLE);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtServicePort, refreshTokenRepositoryPort);
    }
}
