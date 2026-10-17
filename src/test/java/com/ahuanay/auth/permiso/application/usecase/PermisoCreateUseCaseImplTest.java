package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.domain.exception.PermisoInvalidoException;
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
class PermisoCreateUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoCreateUseCaseImpl permisoCreateUseCase;

    private PermisoCommand command;
    private Permiso permisoDomain;

    @BeforeEach
    void setUp() {
        command = new PermisoCommand("PERMISO_CREATE");
        permisoDomain = new Permiso(1L, "PERMISO_CREATE");
    }

    @Test
    @DisplayName("Debería crear un permiso exitosamente cuando no existe duplicado")
    void deberiaCrearPermisoExitosamente() {
        // Given
        when(permisoRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.empty());
        when(permisoRepositoryPort.save(any(Permiso.class))).thenReturn(permisoDomain);

        // When
        PermisoResponse resultado = permisoCreateUseCase.create(command);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nombre()).isEqualTo("PERMISO_CREATE");

        verify(permisoRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, times(1)).save(any(Permiso.class));
    }

    @Test
    @DisplayName("Debería lanzar PermisoDuplicadoException cuando el nombre del permiso ya existe")
    void deberiaLanzarExcepcionCuandoPermisoDuplicado() {
        // Given
        when(permisoRepositoryPort.findByNombre(command.nombre())).thenReturn(Optional.of(permisoDomain));

        // When / Then
        assertThatThrownBy(() -> permisoCreateUseCase.create(command))
                .isInstanceOf(PermisoDuplicadoException.class)
                .hasMessage("El permiso 'PERMISO_CREATE' ya existe.");

        verify(permisoRepositoryPort, times(1)).findByNombre(command.nombre());
        verify(permisoRepositoryPort, never()).save(any(Permiso.class));
    }

    @Test
    @DisplayName("Debería lanzar PermisoInvalidoException cuando el nombre del comando es inválido")
    void deberiaLanzarExcepcionCuandoNombreInvalido() {
        // Given
        PermisoCommand comandoInvalido = new PermisoCommand("");

        // When / Then
        assertThatThrownBy(() -> permisoCreateUseCase.create(comandoInvalido))
                .isInstanceOf(PermisoInvalidoException.class)
                .hasMessage("El nombre del permiso no puede estar vacío.");

        verify(permisoRepositoryPort, never()).findByNombre(anyString());
        verify(permisoRepositoryPort, never()).save(any(Permiso.class));
    }
}