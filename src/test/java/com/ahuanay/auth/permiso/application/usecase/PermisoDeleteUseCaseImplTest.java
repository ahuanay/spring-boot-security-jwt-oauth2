package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("Debería eliminar permiso exitosamente")
    void deberiaEliminarPermisoExitosamente() {
        // Given
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoDomain));

        // When
        permisoDeleteUseCase.delete(id);

        // Then
        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, times(1)).deleteById(permisoDomain.getId());
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarExcepcionSiPermisoNoExiste() {
        // Given
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> permisoDeleteUseCase.delete(id))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Permiso no encontrado con ID: " + id);

        verify(permisoRepositoryPort, times(1)).findById(id);
        verify(permisoRepositoryPort, never()).deleteById(anyLong());
    }
}