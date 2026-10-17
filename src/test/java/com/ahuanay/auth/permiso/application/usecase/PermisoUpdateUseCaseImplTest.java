package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.model.Permiso;
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
class PermisoUpdateUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoUpdateUseCaseImpl permisoUpdateUseCase;

    private Long id;
    private PermisoCommand command;
    private Permiso permisoExistente;

    @BeforeEach
    void setUp() {
        id = 1L;
        command = new PermisoCommand("PERMISO_UPDATE");
        permisoExistente = new Permiso(id, "PERMISO_CREATE");
    }

    @Test
    @DisplayName("Debería actualizar permiso exitosamente")
    void deberiaActualizarPermisoExitosamente() {
        // Given
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoExistente));
        when(permisoRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        when(permisoRepositoryPort.save(any(Permiso.class))).thenReturn(new Permiso(id, command.nombre()));

        // When
        PermisoResponse resultado = permisoUpdateUseCase.update(id, command);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(id);
        assertThat(resultado.nombre()).isEqualTo("PERMISO_UPDATE");

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, times(1)).save(any(Permiso.class));
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarExcepcionCuandoIdNoExiste() {
        // Given
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> permisoUpdateUseCase.update(id, command))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, never()).findByNombre(anyString());
        verify(permisoRepositoryPort, never()).save(any(Permiso.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando ya existe otro permiso con el mismo nombre")
    void deberiaLanzarExcepcionCuandoNombreDuplicado() {
        // Given
        Permiso otroPermiso = new Permiso(2L, "PERMISO_UPDATE"); // ID diferente pero mismo nombre

        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoExistente));
        when(permisoRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.of(otroPermiso));

        // When / Then
        assertThatThrownBy(() -> permisoUpdateUseCase.update(id, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe otro permiso con el nombre: " + command.nombre());

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, never()).save(any(Permiso.class));
    }
}