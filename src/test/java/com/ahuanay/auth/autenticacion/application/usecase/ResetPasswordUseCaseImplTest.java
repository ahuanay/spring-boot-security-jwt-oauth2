package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ResetPasswordCommand;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.support.TestTokens;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ResetPasswordUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_PLANO = "3f9a1c0b7d2e5f8a4c6b9d0e1f2a3b4c5d6e7f809a1b2c3d4e5f60718293a4b5";
    private static final String TOKEN_HASH = "hash-sha-256";
    private static final String NUEVA_PASSWORD = "NewSecurePassword123*";
    private static final String NUEVA_PASSWORD_HASHEADA = "bcrypt-hash";

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private ResetPasswordCommand command;
    private Usuario usuario;
    private PasswordResetToken tokenVigente;
    private PasswordResetToken tokenUsado;
    private PasswordResetToken tokenExpirado;
    private PasswordResetToken tokenConsumido;

    @BeforeEach
    void setUp() {
        command = new ResetPasswordCommand(TOKEN_PLANO, NUEVA_PASSWORD);
        usuario = TestUsuarios.local();
        tokenVigente = TestTokens.resetToken(TOKEN_HASH, EMAIL, LocalDateTime.now().plusMinutes(30));
        tokenUsado = TestTokens.resetTokenDetalle(1L, TOKEN_HASH, EMAIL,
            LocalDateTime.now().plusMinutes(30), LocalDateTime.now().minusMinutes(1));
        tokenExpirado = TestTokens.resetToken(TOKEN_HASH, EMAIL, LocalDateTime.now().minusMinutes(1));
        tokenConsumido = TestTokens.resetTokenUsado(TOKEN_HASH, EMAIL, LocalDateTime.now().plusMinutes(30));
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException y no alterar nada cuando la nueva contraseña no cumple la política")
    void deberiaLanzarUsuarioInvalidoExceptionYNoAlterarNada_CuandoLaNuevaContrasenaNoCumpleLaPolitica() {
        // Arrange
        ResetPasswordCommand comandoContrasenaDebil = new ResetPasswordCommand(TOKEN_PLANO, "abc12");

        // Act & Assert
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(comandoContrasenaDebil))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage(ReglaContrasena.MENSAJE);

        verifyNoInteractions(resetTokenGeneratorPort, passwordResetTokenRepositoryPort,
            usuarioRepositoryPort, refreshTokenRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería restablecer la contraseña, consumir el token y revocar las sesiones activas cuando los datos son válidos")
    void deberiaRestablecerContrasenaConsumirTokenYRevocarSesiones_CuandoLosDatosSonValidos() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenVigente));
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.encode(NUEVA_PASSWORD)).thenReturn(NUEVA_PASSWORD_HASHEADA);
        when(passwordResetTokenRepositoryPort.save(any(PasswordResetToken.class))).thenReturn(tokenConsumido);

        // Act
        MensajeResponse response = resetPasswordUseCase.resetPassword(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje())
            .isEqualTo("La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales.");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort, times(1)).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getPassword()).isEqualTo(NUEVA_PASSWORD_HASHEADA);
        assertThat(usuarioCaptor.getValue().getId()).isEqualTo(1L);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepositoryPort, times(1)).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().estaUsado()).isTrue();

        verify(refreshTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(passwordResetTokenRepositoryPort, never()).deleteByTokenHash(any());
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException y no alterar nada cuando el token no existe")
    void deberiaLanzarCredencialInvalidoExceptionYNoAlterarNada_CuandoElTokenNoExiste() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de recuperación es inválido o expirado.");

        verify(passwordResetTokenRepositoryPort, never()).save(any(PasswordResetToken.class));
        verify(passwordResetTokenRepositoryPort, never()).deleteByTokenHash(any());
        verifyNoInteractions(usuarioRepositoryPort, refreshTokenRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException y no alterar nada cuando el token ya fue utilizado")
    void deberiaLanzarCredencialInvalidoExceptionYNoAlterarNada_CuandoElTokenYaFueUtilizado() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenUsado));

        // Act & Assert
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de recuperación es inválido o expirado.");

        verify(passwordResetTokenRepositoryPort, never()).save(any(PasswordResetToken.class));
        verify(passwordResetTokenRepositoryPort, never()).deleteByTokenHash(any());
        verifyNoInteractions(usuarioRepositoryPort, refreshTokenRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería revocar el token expirado y lanzar CredencialInvalidoException cuando el token ha expirado")
    void deberiaRevocarTokenExpiradoYLanzarCredencialInvalidoException_CuandoElTokenHaExpirado() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenExpirado));

        // Act & Assert
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de recuperación es inválido o expirado.");

        verify(passwordResetTokenRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(passwordResetTokenRepositoryPort, never()).save(any(PasswordResetToken.class));
        verifyNoInteractions(usuarioRepositoryPort, refreshTokenRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException sin consumir el token cuando el usuario ya no existe")
    void deberiaLanzarCredencialInvalidoExceptionSinConsumirElToken_CuandoElUsuarioYaNoExiste() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(tokenVigente));
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Usuario no encontrado.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
        verify(passwordResetTokenRepositoryPort, never()).save(any(PasswordResetToken.class));
        verify(refreshTokenRepositoryPort, never()).deleteByEmail(any());
        verify(passwordHasherPort, never()).encode(any());
    }
}