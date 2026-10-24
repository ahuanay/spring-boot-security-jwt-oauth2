package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.domain.exception.PermisoEnUsoException;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoDeleteUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoDeleteUseCaseImpl permisoDeleteUseCase;

    private Long id;
    private Permiso permisoDomain;

    @BeforeEach
    void setUp() {
        id = 1L;
        permisoDomain = new Permiso(id, "PERMISO_CREATE");
    }

    @Test
    @DisplayName("Debería eliminar el permiso exitosamente cuando el permiso existe y no está asignado a roles")
    void deberiaEliminarPermisoExitosamente_CuandoElPermisoExisteYNoEstaAsignadoARoles() {
        // Arrange
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoDomain));
        when(permisoRepositoryPort.findRolIdsByPermisoId(id)).thenReturn(List.of());

        // Act
        permisoDeleteUseCase.delete(id);

        // Assert
        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, times(1)).findRolIdsByPermisoId(id);
        verify(permisoRepositoryPort, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar PermisoEnUsoException cuando el permiso está asignado a roles")
    void deberiaLanzarPermisoEnUsoException_CuandoElPermisoEstaAsignadoARoles() {
        // Arrange
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoDomain));
        when(permisoRepositoryPort.findRolIdsByPermisoId(id)).thenReturn(List.of(1L, 2L));

        // Act & Assert
        assertThatThrownBy(() -> permisoDeleteUseCase.delete(id))
            .isInstanceOf(PermisoEnUsoException.class)
            .hasMessage("El permiso no puede eliminarse porque está asignado a los roles con ID: 1, 2");

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, times(1)).findRolIdsByPermisoId(id);
        verify(permisoRepositoryPort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElIdNoExiste() {
        // Arrange
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> permisoDeleteUseCase.delete(id))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("No existe el permiso solicitado");

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, never()).deleteById(anyLong());
    }
}