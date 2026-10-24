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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoGetByIdUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoGetByIdUseCaseImpl permisoGetByIdUseCase;

    private Long id;
    private Permiso permisoDomain;

    @BeforeEach
    void setUp() {
        id = 1L;
        permisoDomain = new Permiso(id, "PERMISO_CREATE");
    }

    @Test
    @DisplayName("Debería obtener el permiso exitosamente cuando el ID existe")
    void deberiaObtenerPermisoExitosamente_CuandoElIdExiste() {
        // Arrange
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.of(permisoDomain));

        // Act
        var resultado = permisoGetByIdUseCase.getById(id);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(id);
        verify(permisoRepositoryPort, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debería lanzar RecursoNoEncontradoException cuando el ID no existe")
    void deberiaLanzarRecursoNoEncontradoException_CuandoElIdNoExiste() {
        // Arrange
        when(permisoRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> permisoGetByIdUseCase.getById(id))
            .isInstanceOf(RecursoNoEncontradoException.class)
            .hasMessage("No existe el permiso solicitado");

        verify(permisoRepositoryPort, times(1)).findById(id);
    }
}