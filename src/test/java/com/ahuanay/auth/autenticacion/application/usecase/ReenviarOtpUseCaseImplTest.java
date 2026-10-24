package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ReenviarOtpCommand;
import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.support.TestTokens;
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
class ReenviarOtpUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_MFA = "mfa-token-789";
    private static final String TOKEN_HASH = "hash-token";
    private static final int EXPIRACION_MINUTOS = 5;
    private static final int COOLDOWN_SEGUNDOS = 60;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private ReenviarOtpUseCaseImpl reenviarOtpUseCase;

    private ReenviarOtpCommand command;

    @BeforeEach
    void setUp() {
        command = new ReenviarOtpCommand(TOKEN_MFA);
        ReflectionTestUtils.setField(reenviarOtpUseCase, "otpExpirationMinutes", EXPIRACION_MINUTOS);
        ReflectionTestUtils.setField(reenviarOtpUseCase, "resendCooldownSeconds", COOLDOWN_SEGUNDOS);
    }

    @Test
    @DisplayName("Debería generar y enviar un código nuevo cuando el reenvío está fuera del tiempo de espera")
    void deberiaGenerarYEnviarCodigoNuevo_CuandoElReenvioEstaFueraDelTiempoDeEspera() {
        // Arrange: emitido hace más de 60 segundos
        MfaOtp otp = TestTokens.otp(
            TOKEN_HASH, EMAIL, "hash-codigo-viejo", PropositoOtp.LOGIN,
            LocalDateTime.now().minusMinutes(1).plusMinutes(EXPIRACION_MINUTOS));
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));
        when(resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO)).thenReturn("482915");
        when(resetTokenGeneratorPort.hash("482915")).thenReturn("hash-codigo-nuevo");

        // Act
        MensajeResponse response = reenviarOtpUseCase.reenviarOtp(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo("Generamos un nuevo código y lo enviamos a tu correo.");
        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(mfaOtpRepositoryPort, times(1)).save(any(MfaOtp.class));
        verify(emailSenderPort, times(1)).enviarOtpAcceso(EMAIL, "482915", EXPIRACION_MINUTOS);
    }

    @Test
    @DisplayName("Debería lanzar OtpBloqueadoException cuando el reenvío se solicita antes del tiempo de espera")
    void deberiaLanzarOtpBloqueadoException_CuandoElReenvioSeSolicitaAntesDelTiempoDeEspera() {
        // Arrange: emitido hace menos de 60 segundos
        MfaOtp otp = TestTokens.otp(
            TOKEN_HASH, EMAIL, "hash-codigo", PropositoOtp.LOGIN,
            LocalDateTime.now().plusSeconds(30).plusMinutes(EXPIRACION_MINUTOS));
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));

        // Act & Assert
        assertThatThrownBy(() -> reenviarOtpUseCase.reenviarOtp(command))
            .isInstanceOf(OtpBloqueadoException.class)
            .hasMessage("Debes esperar 60 segundos para solicitar un nuevo código.");

        verify(mfaOtpRepositoryPort, never()).deleteByTokenHash(anyString());
        verifyNoInteractions(emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token intermedio no existe")
    void deberiaLanzarCredencialInvalidoException_CuandoElTokenIntermedioNoExiste() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reenviarOtpUseCase.reenviarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verifyNoInteractions(emailSenderPort);
    }

    @Test
    @DisplayName("Debería lançar CredencialInvalidoException y eliminar el desafío cuando el OTP ya expiró")
    void deberiaLanzarCredencialInvalidoExceptionYEliminarElDesafio_CuandoElOtpYaExpiro() {
        // Arrange
        MfaOtp otpExpirado = TestTokens.otp(
            TOKEN_HASH, EMAIL, "hash-codigo", PropositoOtp.LOGIN, LocalDateTime.now().minusSeconds(1));
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otpExpirado));

        // Act & Assert
        assertThatThrownBy(() -> reenviarOtpUseCase.reenviarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verifyNoInteractions(emailSenderPort);
    }

    @Test
    @DisplayName("Debería lançar CredencialInvalidoException cuando el OTP ya fue utilizado")
    void deberiaLanzarCredencialInvalidoException_CuandoElOtpYaFueUtilizado() {
        // Arrange
        MfaOtp otpUsado = TestTokens.otpCompleto(
            TOKEN_HASH, EMAIL, "hash-codigo", PropositoOtp.LOGIN,
            LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS), 0, LocalDateTime.now());
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otpUsado));

        // Act & Assert
        assertThatThrownBy(() -> reenviarOtpUseCase.reenviarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verifyNoInteractions(emailSenderPort);
    }
}
