package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ActivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.MfaNoDisponibleException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.common.domain.exception.ReglaNegocioException;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivarMfaUseCaseImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "SecurePassword123*";
    private static final String HASH = "$2a$10$hashSimulado";
    private static final int EXPIRACION_MINUTOS = 5;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private ActivarMfaUseCaseImpl activarMfaUseCase;

    private ActivarMfaCommand command;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        command = new ActivarMfaCommand(USUARIO_ID, PASSWORD);
        usuario = TestUsuarios.local();
        ReflectionTestUtils.setField(activarMfaUseCase, "otpExpirationMinutes", EXPIRACION_MINUTOS);
    }

    @Test
    @DisplayName("Debería generar y enviar el código de activación sin activar el doble factor todavía")
    void deberiaGenerarYEnviarCodigoDeActivacion_SinActivarElDobleFactorTodavia() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(true);
        when(resetTokenGeneratorPort.generarToken()).thenReturn("token-aleatorio");
        when(resetTokenGeneratorPort.hash("token-aleatorio")).thenReturn("hash-token");
        when(resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO)).thenReturn("482915");
        when(resetTokenGeneratorPort.hash("482915")).thenReturn("hash-codigo");

        // Act
        MensajeResponse response = activarMfaUseCase.activarMfa(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo(
            "Enviamos un código de activación a tu correo. Introdúcelo para finalizar la activación del doble factor.");
        verify(mfaOtpRepositoryPort, times(1)).deleteByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION);
        verify(mfaOtpRepositoryPort, times(1)).save(any(MfaOtp.class));
        verify(emailSenderPort, times(1)).enviarOtpActivacion(EMAIL, "482915", EXPIRACION_MINUTOS);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar MfaNoDisponibleException cuando el usuario es de un proveedor externo")
    void deberiaLanzarMfaNoDisponibleException_CuandoElUsuarioEsDeUnProveedorExterno() {
        // Arrange
        Usuario usuarioGoogle = TestUsuarios.google();
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioGoogle));

        // Act & Assert
        assertThatThrownBy(() -> activarMfaUseCase.activarMfa(command))
            .isInstanceOf(MfaNoDisponibleException.class)
            .hasMessage("Los usuarios registrados mediante Google/GitHub no requieren doble factor de autenticación.");

        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar ReglaNegocioException cuando el doble factor ya está activado")
    void deberiaLanzarReglaNegocioException_CuandoElDobleFactorYaEstaActivado() {
        // Arrange
        Usuario usuarioConMfa = TestUsuarios.localConMfa();
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioConMfa));

        // Act & Assert
        assertThatThrownBy(() -> activarMfaUseCase.activarMfa(command))
            .isInstanceOf(ReglaNegocioException.class)
            .hasMessage("El doble factor de autenticación ya se encuentra activado.");

        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando la contraseña es incorrecta")
    void deberiaLanzarCredencialInvalidoException_CuandoLaContrasenaEsIncorrecta() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> activarMfaUseCase.activarMfa(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("La contraseña del usuario es incorrecta.");

        verifyNoInteractions(mfaOtpRepositoryPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el usuario no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElUsuarioNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activarMfaUseCase.activarMfa(command))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("Usuario no encontrado.");

        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort, emailSenderPort);
    }
}
