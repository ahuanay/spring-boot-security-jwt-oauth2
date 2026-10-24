package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ForgotPasswordCommand;
import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_PLANO = "3f9a1c0b7d2e5f8a4c6b9d0e1f2a3b4c5d6e7f809a1b2c3d4e5f60718293a4b5";
    private static final String TOKEN_HASH = "hash-sha-256";
    private static final int EXPIRACION_MINUTOS = 30;
    private static final String FRONTEND_URL = "http://localhost:4200/auth/restablecer-contrasena";
    private static final String MENSAJE_GENERICO =
        "Si el correo está registrado, recibirás un enlace para restablecer la contraseña.";

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private ForgotPasswordUseCaseImpl forgotPasswordUseCase;

    private ForgotPasswordCommand command;
    private Usuario usuarioLocal;
    private PasswordResetToken tokenGuardado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(forgotPasswordUseCase, "expirationMinutes", EXPIRACION_MINUTOS);
        ReflectionTestUtils.setField(forgotPasswordUseCase, "frontendUrl", FRONTEND_URL);

        command = new ForgotPasswordCommand(EMAIL);
        usuarioLocal = TestUsuarios.local();
        tokenGuardado = TestTokens.resetToken(TOKEN_HASH, EMAIL,
            LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS));
    }

    @Test
    @DisplayName("Debería generar, persistir con hash y enviar el link de recuperación para un usuario LOCAL")
    void deberiaGenerarPersistirYEnviarLink_CuandoElUsuarioEsLocal() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioLocal));
        when(resetTokenGeneratorPort.generarToken()).thenReturn(TOKEN_PLANO);
        when(resetTokenGeneratorPort.hash(TOKEN_PLANO)).thenReturn(TOKEN_HASH);
        when(passwordResetTokenRepositoryPort.save(any(PasswordResetToken.class))).thenReturn(tokenGuardado);

        // Act
        MensajeResponse response = forgotPasswordUseCase.forgotPassword(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo(MENSAJE_GENERICO);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(passwordResetTokenRepositoryPort, times(1)).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(TOKEN_PLANO);
        assertThat(tokenCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(tokenCaptor.getValue().getExpiresAt()).isAfter(LocalDateTime.now())
            .isBefore(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS + 1));

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort, times(1)).enviarRecuperacionContrasena(eq(EMAIL), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith(FRONTEND_URL).contains("token=" + TOKEN_PLANO);
    }

    @Test
    @DisplayName("Debería responder el mensaje genérico sin generar tokens cuando el email no existe")
    void deberiaResponderMensajeGenericoSinGenerarTokens_CuandoElEmailNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act
        MensajeResponse response = forgotPasswordUseCase.forgotPassword(command);

        // Assert
        assertThat(response.mensaje()).isEqualTo(MENSAJE_GENERICO);

        verifyNoInteractions(passwordResetTokenRepositoryPort, resetTokenGeneratorPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería responder mensaje explícito sin enviar email cuando el usuario es de Google")
    void deberiaResponderMensajeExplicitoSinEnviarEmail_CuandoElUsuarioEsDeGoogle() {
        // Arrange
        Usuario usuarioGoogle = TestUsuarios.google();
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioGoogle));

        // Act
        MensajeResponse response = forgotPasswordUseCase.forgotPassword(command);

        // Assert
        assertThat(response.mensaje())
            .contains("Google")
            .contains("Inicia sesión con ese proveedor");

        verifyNoInteractions(passwordResetTokenRepositoryPort, resetTokenGeneratorPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería responder mensaje explícito sin enviar email cuando el usuario es de GitHub")
    void deberiaResponderMensajeExplicitoSinEnviarEmail_CuandoElUsuarioEsDeGitHub() {
        // Arrange
        Usuario usuarioGitHub = TestUsuarios.github();
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioGitHub));

        // Act
        MensajeResponse response = forgotPasswordUseCase.forgotPassword(command);

        // Assert
        assertThat(response.mensaje())
            .contains("GitHub")
            .contains("Inicia sesión con ese proveedor");

        verifyNoInteractions(passwordResetTokenRepositoryPort, resetTokenGeneratorPort, emailSenderPort);
    }
}
