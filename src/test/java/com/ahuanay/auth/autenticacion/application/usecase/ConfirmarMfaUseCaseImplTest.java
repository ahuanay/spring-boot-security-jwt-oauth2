package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ConfirmarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.MfaNoDisponibleException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmarMfaUseCaseImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final String EMAIL = "user@email.com";
    private static final String CODIGO = "482915";
    private static final String CODIGO_HASH = "hash-codigo";
    private static final String TOKEN_HASH = "hash-token";
    private static final int MAX_ATTEMPTS = 5;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @InjectMocks
    private ConfirmarMfaUseCaseImpl confirmarMfaUseCase;

    private ConfirmarMfaCommand command;
    private Usuario usuario;
    private MfaOtp otpActivacion;

    @BeforeEach
    void setUp() {
        command = new ConfirmarMfaCommand(USUARIO_ID, CODIGO);
        usuario = TestUsuarios.local();
        otpActivacion = TestTokens.otp(TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.ACTIVACION,
            LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(confirmarMfaUseCase, "maxAttempts", MAX_ATTEMPTS);
    }

    @Test
    @DisplayName("Debería activar el doble factor y eliminar el código de activación cuando el código es correcto")
    void deberiaActivarElDobleFactorYEliminarElCodigoDeActivacion_CuandoElCodigoEsCorrecto() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(mfaOtpRepositoryPort.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.of(otpActivacion));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn(CODIGO_HASH);

        // Act
        MensajeResponse response = confirmarMfaUseCase.confirmarMfa(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo(
            "El doble factor de autenticación quedó activado. El próximo inicio de sesión te pedirá el código de acceso.");
        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(usuarioRepositoryPort, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar OtpInvalidoException cuando no hay un código de activación pendiente")
    void deberiaLanzarOtpInvalidoException_CuandoNoHayUnCodigoDeActivacionPendiente() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(mfaOtpRepositoryPort.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> confirmarMfaUseCase.confirmarMfa(command))
            .isInstanceOf(OtpInvalidoException.class)
            .hasMessage("No hay un código de activación pendiente. Inicia la activación nuevamente.");

        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar OtpInvalidoException y eliminar el código cuando el código ya expiró")
    void deberiaLanzarOtpInvalidoExceptionYEliminarElCodigo_CuandoElCodigoYaExpiro() {
        // Arrange
        MfaOtp otpExpirado = TestTokens.otp(
            TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.ACTIVACION, LocalDateTime.now().minusSeconds(1));
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(mfaOtpRepositoryPort.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.of(otpExpirado));

        // Act & Assert
        assertThatThrownBy(() -> confirmarMfaUseCase.confirmarMfa(command))
            .isInstanceOf(OtpInvalidoException.class)
            .hasMessage("El código de activación expiró. Inicia la activación nuevamente.");

        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería registrar el intento fallido y lanzar OtpInvalidoException cuando el código es incorrecto")
    void deberiaRegistrarElIntentoFallidoYLanzarOtpInvalidoException_CuandoElCodigoEsIncorrecto() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(mfaOtpRepositoryPort.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.of(otpActivacion));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn("otro-hash");

        // Act & Assert
        assertThatThrownBy(() -> confirmarMfaUseCase.confirmarMfa(command))
            .isInstanceOf(OtpInvalidoException.class)
            .hasMessage("El código de activación es incorrecto. Intentos restantes: 4.");

        verify(mfaOtpRepositoryPort, times(1)).save(otpActivacion);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería eliminar el código y lanzar OtpBloqueadoException cuando se agotan los intentos de activación")
    void deberiaEliminarElCodigoYLanzarOtpBloqueadoException_CuandoSeAgotanLosIntentosDeActivacion() {
        // Arrange
        MfaOtp otpUltimoIntento = TestTokens.otpCompleto(
            TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.ACTIVACION, LocalDateTime.now().plusMinutes(5),
            MAX_ATTEMPTS - 1, null);
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(mfaOtpRepositoryPort.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.of(otpUltimoIntento));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn("otro-hash");

        // Act & Assert
        assertThatThrownBy(() -> confirmarMfaUseCase.confirmarMfa(command))
            .isInstanceOf(OtpBloqueadoException.class)
            .hasMessage("Se superó el número máximo de intentos. Inicia la activación nuevamente.");

        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(mfaOtpRepositoryPort, never()).save(any(MfaOtp.class));
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar MfaNoDisponibleException cuando el usuario es de un proveedor externo")
    void deberiaLanzarMfaNoDisponibleException_CuandoElUsuarioEsDeUnProveedorExterno() {
        // Arrange
        Usuario usuarioGoogle = TestUsuarios.google();
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioGoogle));

        // Act & Assert
        assertThatThrownBy(() -> confirmarMfaUseCase.confirmarMfa(command))
            .isInstanceOf(MfaNoDisponibleException.class)
            .hasMessage("Los usuarios registrados mediante Google/GitHub no pueden activar el doble factor de autenticación.");

        verifyNoInteractions(mfaOtpRepositoryPort, resetTokenGeneratorPort);
    }
}