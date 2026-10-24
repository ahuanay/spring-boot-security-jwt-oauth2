package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.domain.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolDeleteUseCaseImplTest {

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @InjectMocks
    private RolDeleteUseCaseImpl rolDeleteUseCase;

    private Long id;
    private Rol rolDomain;

    @BeforeEach
    void setUp() {
        id = 1L;
        rolDomain = new Rol(id, "DOCTOR", Set.of(new Permiso(1L, "PERMISO_CREATE")));
    }

    @Test
    @DisplayName("Debería eliminar el rol exitosamente cuando el rol existe")
    void deberiaEliminarRolExitosamente_CuandoElRolExiste() {
        // Arrange
        when(rolRepositoryPort.findById(id)).thenReturn(Optional.of(rolDomain));

        // Act
        rolDeleteUseCase.delete(id);

        // Assert
        verify(rolRepositoryPort, times(1)).findById(id);
        verify(rolRepositoryPort, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElIdNoExiste() {
        // Arrange
        when(rolRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rolDeleteUseCase.delete(id))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("No existe el rol solicitado");

        verify(rolRepositoryPort, times(1)).findById(id);
        verify(rolRepositoryPort, never()).deleteById(anyLong());
    }
}