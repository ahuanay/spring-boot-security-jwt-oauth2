package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.RefreshTokenCommand;
import com.ahuanay.auth.autenticacion.application.port.out.JwtServicePort;
import com.ahuanay.auth.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.support.TestTokens;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_ANTIGUO = "old-refresh-token";

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private JwtServicePort jwtServicePort;

    @InjectMocks
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    private RefreshTokenCommand command;
    private RefreshToken tokenAlmacenado;
    private RefreshToken tokenAlmacenadoExpirado;
    private LocalDateTime expiracionFutura;
    private Usuario usuario;
    private RefreshTokenGenerado refreshTokenGenerado;

    @BeforeEach
    void setUp() {
        command = new RefreshTokenCommand(TOKEN_ANTIGUO);
        expiracionFutura = TestTokens.expiracionFutura();
        tokenAlmacenado = TestTokens.refreshTokenValido(TOKEN_ANTIGUO, EMAIL);
        tokenAlmacenadoExpirado = TestTokens.refreshTokenExpirado(TOKEN_ANTIGUO, EMAIL);
        usuario = TestUsuarios.local();
        refreshTokenGenerado = new RefreshTokenGenerado("new-refresh-token", expiracionFutura);
    }

    @Test
    @DisplayName("Debería rotar y retornar nuevos tokens cuando el refresh token es válido")
    void deberiaRotarYRetornarNuevosTokens_CuandoElRefreshTokenEsValido() {
        // Arrange
        when(refreshTokenRepositoryPort.findByToken(TOKEN_ANTIGUO)).thenReturn(Optional.of(tokenAlmacenado));
        when(jwtServicePort.extractUsername(TOKEN_ANTIGUO)).thenReturn(EMAIL);
        when(jwtServicePort.isTokenValid(TOKEN_ANTIGUO, EMAIL)).thenReturn(true);
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtServicePort.generateAccessToken(usuario)).thenReturn("new-access-token");
        when(jwtServicePort.generateRefreshToken(usuario)).thenReturn(refreshTokenGenerado);

        // Act
        AuthResponse response = refreshTokenUseCase.refreshToken(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(refreshTokenRepositoryPort, times(1)).findByToken(TOKEN_ANTIGUO);
        verify(jwtServicePort, times(1)).extractUsername(TOKEN_ANTIGUO);
        verify(jwtServicePort, times(1)).isTokenValid(TOKEN_ANTIGUO, EMAIL);
        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(jwtServicePort, times(1)).generateAccessToken(usuario);
        verify(jwtServicePort, times(1)).generateRefreshToken(usuario);
        verify(refreshTokenRepositoryPort, times(1)).deleteByToken(TOKEN_ANTIGUO);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el refresh token no existe en la base de datos")
    void deberiaLanzarCredencialInvalidoException_CuandoElRefreshTokenNoExiste() {
        // Arrange
        when(refreshTokenRepositoryPort.findByToken(TOKEN_ANTIGUO)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.refreshToken(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Refresh token inválido o expirado.");

        verify(refreshTokenRepositoryPort, times(1)).findByToken(TOKEN_ANTIGUO);
        verifyNoInteractions(jwtServicePort, usuarioRepositoryPort);
        verify(refreshTokenRepositoryPort, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería revocar el token y lanzar CredencialInvalidoException cuando su expiración del lado del servidor ya pasó")
    void deberiaRevocarTokenYLanzarExcepcion_CuandoSuExpiracionEnLaBaseDeDatosYaPaso() {
        // Arrange
        when(refreshTokenRepositoryPort.findByToken(TOKEN_ANTIGUO)).thenReturn(Optional.of(tokenAlmacenadoExpirado));

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.refreshToken(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Refresh token inválido o expirado.");

        verify(refreshTokenRepositoryPort, times(1)).findByToken(TOKEN_ANTIGUO);
        verify(refreshTokenRepositoryPort, times(1)).deleteByToken(TOKEN_ANTIGUO);
        verifyNoInteractions(jwtServicePort, usuarioRepositoryPort);
        verify(refreshTokenRepositoryPort, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería revocar el token y lanzar CredencialInvalidoException cuando el JWT ya no es válido")
    void deberiaRevocarTokenYLanzarExcepcion_CuandoElJwtYaNoEsValido() {
        // Arrange
        when(refreshTokenRepositoryPort.findByToken(TOKEN_ANTIGUO)).thenReturn(Optional.of(tokenAlmacenado));
        when(jwtServicePort.extractUsername(TOKEN_ANTIGUO)).thenReturn(EMAIL);
        when(jwtServicePort.isTokenValid(TOKEN_ANTIGUO, EMAIL)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.refreshToken(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Refresh token inválido o expirado.");

        verify(refreshTokenRepositoryPort, times(1)).findByToken(TOKEN_ANTIGUO);
        verify(jwtServicePort, times(1)).extractUsername(TOKEN_ANTIGUO);
        verify(jwtServicePort, times(1)).isTokenValid(TOKEN_ANTIGUO, EMAIL);
        verify(refreshTokenRepositoryPort, times(1)).deleteByToken(TOKEN_ANTIGUO);
        verify(refreshTokenRepositoryPort, never()).save(any(RefreshToken.class));
        verify(usuarioRepositoryPort, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el usuario del token ya no existe")
    void deberiaLanzarCredencialInvalidoException_CuandoElUsuarioDelTokenYaNoExiste() {
        // Arrange
        when(refreshTokenRepositoryPort.findByToken(TOKEN_ANTIGUO)).thenReturn(Optional.of(tokenAlmacenado));
        when(jwtServicePort.extractUsername(TOKEN_ANTIGUO)).thenReturn(EMAIL);
        when(jwtServicePort.isTokenValid(TOKEN_ANTIGUO, EMAIL)).thenReturn(true);
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.refreshToken(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Usuario no encontrado.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(refreshTokenRepositoryPort, never()).deleteByToken(any());
        verify(refreshTokenRepositoryPort, never()).save(any(RefreshToken.class));
    }
}
