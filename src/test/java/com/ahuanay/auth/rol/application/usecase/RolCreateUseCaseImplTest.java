package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.exception.RolDuplicadoException;
import com.ahuanay.auth.rol.domain.exception.RolInvalidoException;
import com.ahuanay.auth.rol.domain.model.Rol;
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
class RolCreateUseCaseImplTest {

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private RolCreateUseCaseImpl rolCreateUseCase;

    private RolCommand command;
    private Rol rolDomain;
    private Permiso permiso1;
    private Permiso permiso2;

    @BeforeEach
    void setUp() {
        command = new RolCommand("ROL_ADMIN", Set.of(1L, 2L));
        permiso1 = new Permiso(1L, "PERMISO_1");
        permiso2 = new Permiso(2L, "PERMISO_2");
        rolDomain = new Rol(100L, "ROL_ADMIN", Set.of(permiso1, permiso2));
    }

    @Test
    @DisplayName("Debería crear un rol exitosamente cuando los datos son válidos")
    void deberiaCrearRolExitosamente_CuandoDatosSonValidos() {
        // Arrange
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        when(permisoRepositoryPort.findAllByIdIn(command.permisoIds())).thenReturn(List.of(permiso1, permiso2));
        when(rolRepositoryPort.save(any(Rol.class))).thenReturn(rolDomain);

        // Act
        RolResponse response = rolCreateUseCase.create(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.nombre()).isEqualTo("ROL_ADMIN");
        assertThat(response.permisos()).hasSize(2);
        assertThat(response.permisos()).extracting("id").containsExactlyInAnyOrder(1L, 2L);

        verify(rolRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, times(1)).findAllByIdIn(command.permisoIds());
        verify(rolRepositoryPort, times(1)).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RolDuplicadoException cuando el nombre del rol ya existe")
    void deberiaLanzarRolDuplicadoException_CuandoNombreRolYaExiste() {
        // Arrange
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.of(rolDomain));

        // Act & Assert
        assertThatThrownBy(() -> rolCreateUseCase.create(command))
            .isInstanceOf(RolDuplicadoException.class)
            .hasMessage("El rol 'ROL_ADMIN' ya existe.");

        verify(rolRepositoryPort, times(1)).findByNombre(command.nombre());
        verifyNoInteractions(permisoRepositoryPort);
        verify(rolRepositoryPort, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando uno o más permisos no existen")
    void deberiaLanzarRecursoNoEncontradoException_CuandoPermisosNoExisten() {
        // Arrange
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        // Simulamos que solo encuentra un permiso de los dos solicitados
        when(permisoRepositoryPort.findAllByIdIn(command.permisoIds())).thenReturn(List.of(permiso1));

        // Act & Assert
        assertThatThrownBy(() -> rolCreateUseCase.create(command))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessageContaining("Uno o más permisos proporcionados no existen.");

        verify(rolRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, times(1)).findAllByIdIn(command.permisoIds());
        verify(rolRepositoryPort, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando el nombre está vacío")
    void deberiaLanzarRolInvalidoException_CuandoNombreEstaVacio() {
        // Arrange
        RolCommand comandoInvalido = new RolCommand("", Set.of(1L));

        // Act & Assert
        assertThatThrownBy(() -> rolCreateUseCase.create(comandoInvalido))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El nombre del rol no puede estar vacío.");

        verifyNoInteractions(rolRepositoryPort);
        verifyNoInteractions(permisoRepositoryPort);
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando no hay permisos")
    void deberiaLanzarRolInvalidoException_CuandoNoHayPermisos() {
        // Arrange
        RolCommand comandoSinPermisos = new RolCommand("ROL_SIN_PERMISOS", Collections.emptySet());

        // Act & Assert
        assertThatThrownBy(() -> rolCreateUseCase.create(comandoSinPermisos))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El rol debe tener al menos un permiso asignado.");

        verifyNoInteractions(rolRepositoryPort);
        verifyNoInteractions(permisoRepositoryPort);
    }
}