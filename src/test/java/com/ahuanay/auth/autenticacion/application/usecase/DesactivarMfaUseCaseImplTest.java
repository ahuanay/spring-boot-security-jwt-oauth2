package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.DesactivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordHasherPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.support.TestUsuarios;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.common.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesactivarMfaUseCaseImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "SecurePassword123*";
    private static final String HASH = "$2a$10$hashSimulado";

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private DesactivarMfaUseCaseImpl desactivarMfaUseCase;

    private DesactivarMfaCommand command;
    private Usuario usuarioConMfa;

    @BeforeEach
    void setUp() {
        command = new DesactivarMfaCommand(USUARIO_ID, PASSWORD);
        usuarioConMfa = TestUsuarios.localConMfa();
    }

    @Test
    @DisplayName("Debería desactivar el doble factor y cancelar los OTP pendientes cuando la contraseña es correcta")
    void deberiaDesactivarElDobleFactorYCancelarLosOtpPendientes_CuandoLaContrasenaEsCorrecta() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioConMfa));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(true);

        // Act
        MensajeResponse response = desactivarMfaUseCase.desactivarMfa(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo(
            "El doble factor de autenticación quedó desactivado. Inicia sesión solo con tu email y contraseña.");
        verify(usuarioRepositoryPort, times(1)).save(any(Usuario.class));
        verify(mfaOtpRepositoryPort, times(1)).deleteByEmail(EMAIL);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando la contraseña es incorrecta")
    void deberiaLanzarCredencialInvalidoException_CuandoLaContrasenaEsIncorrecta() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioConMfa));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> desactivarMfaUseCase.desactivarMfa(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("La contraseña del usuario es incorrecta.");

        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
        verifyNoInteractions(mfaOtpRepositoryPort);
    }

    @Test
    @DisplayName("Debería lanzar ReglaNegocioException cuando el doble factor no está activado")
    void deberiaLanzarReglaNegocioException_CuandoElDobleFactorNoEstaActivado() {
        // Arrange
        Usuario usuarioSinMfa = TestUsuarios.local();
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioSinMfa));

        // Act & Assert
        assertThatThrownBy(() -> desactivarMfaUseCase.desactivarMfa(command))
            .isInstanceOf(ReglaNegocioException.class)
            .hasMessage("El doble factor de autenticación no está activado.");

        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar ReglaNegocioException cuando el usuario es de un proveedor externo")
    void deberiaLanzarReglaNegocioException_CuandoElUsuarioEsDeUnProveedorExterno() {
        // Arrange
        Usuario usuarioGoogle = TestUsuarios.base().google().conMfa().build();
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioGoogle));

        // Act & Assert
        assertThatThrownBy(() -> desactivarMfaUseCase.desactivarMfa(command))
            .isInstanceOf(ReglaNegocioException.class)
            .hasMessage("Los usuarios registrados mediante Google/GitHub no tienen doble factor de autenticación.");

        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el usuario no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElUsuarioNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findById(USUARIO_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> desactivarMfaUseCase.desactivarMfa(command))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("Usuario no encontrado.");

        verifyNoInteractions(mfaOtpRepositoryPort, passwordHasherPort);
    }
}
