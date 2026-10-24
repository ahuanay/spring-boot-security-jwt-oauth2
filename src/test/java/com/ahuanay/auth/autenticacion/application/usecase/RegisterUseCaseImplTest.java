package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.RegisterCommand;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordHasherPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.UsuarioResponse;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioDuplicadoException;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "SecurePassword123*";

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private RegisterUseCaseImpl registerUseCase;

    private RegisterCommand command;
    private Rol rolPaciente;
    private Usuario usuarioCreado;

    @BeforeEach
    void setUp() {
        command = new RegisterCommand(EMAIL, PASSWORD, Set.of(2L));
        rolPaciente = TestRoles.paciente(2L);
        usuarioCreado = TestUsuarios.base().conPassword("password-hasheada").conRol(rolPaciente).build();
    }

    @Test
    @DisplayName("Debería registrar un usuario exitosamente cuando los datos son válidos")
    void deberiaRegistrarUsuarioExitosamente_CuandoLosDatosSonValidos() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(rolRepositoryPort.findAllByIdIn(Set.of(2L))).thenReturn(List.of(rolPaciente));
        when(passwordHasherPort.encode(PASSWORD)).thenReturn("password-hasheada");
        when(usuarioRepositoryPort.save(any(Usuario.class))).thenReturn(usuarioCreado);

        // Act
        UsuarioResponse response = registerUseCase.register(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.active()).isTrue();
        assertThat(response.roles()).hasSize(1);
        assertThat(response.roles()).extracting("id").containsExactly(2L);
        assertThat(response.roles()).extracting("nombre").containsExactly("PACIENTE");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, times(1)).findAllByIdIn(Set.of(2L));
        verify(passwordHasherPort, times(1)).encode(PASSWORD);
        verify(usuarioRepositoryPort, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar UsuarioDuplicadoException cuando el email ya está registrado")
    void deberiaLanzarUsuarioDuplicadoException_CuandoEmailYaEstaRegistrado() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioCreado));

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(command))
            .isInstanceOf(UsuarioDuplicadoException.class)
            .hasMessage("Ya existe un usuario registrado con el email: " + EMAIL);

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, never()).findAllByIdIn(any());
        verifyNoInteractions(passwordHasherPort);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el rol a asignar no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElRolNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(rolRepositoryPort.findAllByIdIn(Set.of(2L))).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(command))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("Uno o más roles proporcionados no existen.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(rolRepositoryPort, times(1)).findAllByIdIn(Set.of(2L));
        verifyNoInteractions(passwordHasherPort);
        verify(usuarioRepositoryPort, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando el email está vacío")
    void deberiaLanzarUsuarioInvalidoException_CuandoEmailEstaVacio() {
        // Arrange
        RegisterCommand comandoInvalido = new RegisterCommand("", PASSWORD, Set.of(2L));

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(comandoInvalido))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El email del usuario no puede estar vacío.");

        verifyNoInteractions(usuarioRepositoryPort, rolRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando la contraseña no cumple la política de seguridad")
    void deberiaLanzarUsuarioInvalidoException_CuandoLaContrasenaNoCumpleLaPolitica() {
        // Arrange
        RegisterCommand comandoContrasenaDebil = new RegisterCommand(EMAIL, "abc12", Set.of(2L));

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(comandoContrasenaDebil))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage(ReglaContrasena.MENSAJE);

        verifyNoInteractions(usuarioRepositoryPort, rolRepositoryPort, passwordHasherPort);
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando no se asigna ningún rol")
    void deberiaLanzarUsuarioInvalidoException_CuandoNoHayRoles() {
        // Arrange
        RegisterCommand comandoSinRoles = new RegisterCommand(EMAIL, PASSWORD, Collections.emptySet());

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(comandoSinRoles))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario debe tener al menos un rol asignado.");

        verifyNoInteractions(usuarioRepositoryPort, rolRepositoryPort, passwordHasherPort);
    }
}
