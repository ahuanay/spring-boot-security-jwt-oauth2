package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.domain.exception.RolDuplicadoException;
import com.ahuanay.auth.rol.domain.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolUpdateUseCaseImplTest {

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private RolUpdateUseCaseImpl rolUpdateUseCase;

    private Long id;
    private RolCommand command;
    private Rol rolExistente;

    @BeforeEach
    void setUp() {
        id = 1L;
        command = new RolCommand("ROL_UPDATE", Set.of(1L, 2L));
        rolExistente = new Rol(id, "ROL_ORIGINAL", Set.of(new Permiso(1L, "PERMISO_CREATE")));
    }

    private Rol rolActualizadoCon(Permiso... permisos) {
        return new Rol(id, command.nombre(), Set.of(permisos));
    }

    @Test
    @DisplayName("Debería actualizar el rol exitosamente cuando todos los parámetros son válidos")
    void deberiaActualizarRolExitosamente_CuandoTodosLosParametrosSonValidos() {
        // Arrange
        var permiso1 = new Permiso(1L, "PERMISO_CREATE");
        var permiso2 = new Permiso(2L, "PERMISO_DELETE");
        var rolGuardado = rolActualizadoCon(permiso1, permiso2);

        when(rolRepositoryPort.findById(id)).thenReturn(Optional.of(rolExistente));
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        when(permisoRepositoryPort.findAllByIdIn(command.permisoIds())).thenReturn(List.of(permiso1, permiso2));
        when(rolRepositoryPort.save(any(Rol.class))).thenReturn(rolGuardado);

        // Act
        var resultado = rolUpdateUseCase.update(id, command);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(id);
        assertThat(resultado.nombre()).isEqualTo("ROL_UPDATE");
        assertThat(resultado.permisos()).hasSize(2);
        assertThat(resultado.permisos()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
        assertThat(resultado.permisos()).extracting("nombre").containsExactlyInAnyOrder("PERMISO_CREATE", "PERMISO_DELETE");

        verify(rolRepositoryPort, times(1)).findById(id);
        verify(rolRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, times(1)).findAllByIdIn(command.permisoIds());
        verify(rolRepositoryPort, times(1)).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElIdNoExiste() {
        // Arrange
        when(rolRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rolUpdateUseCase.update(id, command))
            .isInstanceOf(RecursoNoEncontradoException.class);

        verify(rolRepositoryPort, times(1)).findById(id);
        verify(rolRepositoryPort, never()).findByNombre(anyString());
        verify(permisoRepositoryPort, never()).findAllByIdIn(anySet());
        verify(rolRepositoryPort, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RolDuplicadoException cuando ya existe otro rol con el mismo nombre")
    void deberiaLanzarRolDuplicadoException_CuandoYaExisteOtroRolConElMismoNombre() {
        // Arrange
        var otroRol = new Rol(2L, command.nombre(), Set.of(new Permiso(1L, "PERMISO_CREATE")));

        when(rolRepositoryPort.findById(id)).thenReturn(Optional.of(rolExistente));
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.of(otroRol));

        // Act & Assert
        assertThatThrownBy(() -> rolUpdateUseCase.update(id, command))
            .isInstanceOf(RolDuplicadoException.class)
            .hasMessage("El rol '" + command.nombre() + "' ya existe.");

        verify(rolRepositoryPort, times(1)).findById(id);
        verify(rolRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, never()).findAllByIdIn(anySet());
        verify(rolRepositoryPort, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería permitir guardar cuando el rol con el mismo nombre es el propio rol")
    void deberiaPermitirGuardar_CuandoElRolConMismoNombreEsElPropioRol() {
        // Arrange
        var mismoRol = new Rol(id, command.nombre(), Set.of(new Permiso(1L, "PERMISO_CREATE")));
        var permiso1 = new Permiso(1L, "PERMISO_CREATE");
        var permiso2 = new Permiso(2L, "PERMISO_DELETE");

        when(rolRepositoryPort.findById(id)).thenReturn(Optional.of(rolExistente));
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.of(mismoRol));
        when(permisoRepositoryPort.findAllByIdIn(command.permisoIds())).thenReturn(List.of(permiso1, permiso2));
        when(rolRepositoryPort.save(any(Rol.class))).thenReturn(rolActualizadoCon(permiso1, permiso2));

        // Act
        var resultado = rolUpdateUseCase.update(id, command);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("ROL_UPDATE");
        verify(rolRepositoryPort, times(1)).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando alguno de los permisos no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoAlgunoDeLosPermisosNoExiste() {
        // Arrange
        var permiso1 = new Permiso(1L, "PERMISO_CREATE");

        when(rolRepositoryPort.findById(id)).thenReturn(Optional.of(rolExistente));
        when(rolRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        when(permisoRepositoryPort.findAllByIdIn(command.permisoIds())).thenReturn(List.of(permiso1));

        // Act & Assert
        assertThatThrownBy(() -> rolUpdateUseCase.update(id, command))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("Uno o más permisos proporcionados no existen.")
            .hasFieldOrPropertyWithValue("notFoundIds", Set.of(2L));

        verify(rolRepositoryPort, never()).save(any(Rol.class));
    }
}