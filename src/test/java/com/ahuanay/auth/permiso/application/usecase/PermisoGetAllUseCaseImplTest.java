package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoGetAllUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoGetAllUseCaseImpl permisoGetAllUseCase;

    @Test
    @DisplayName("Debería retornar una página de permisos exitosamente cuando existen permisos")
    void deberiaRetornarPaginaDePermisosExitosamente_CuandoExistenPermisos() {
        // Arrange
        var query = new PermisoQuery();
        var permisoDomain = new Permiso(1L, "PERMISO_CREATE");
        var springPage = new PageImpl<>(List.of(permisoDomain), PageRequest.of(0, 10), 1);

        doReturn(springPage).when(permisoRepositoryPort).findAll(any(PermisoQuery.class));

        // Act
        var resultado = permisoGetAllUseCase.getAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.content().getFirst().id()).isEqualTo(1L);
        assertThat(resultado.content().getFirst().nombre()).isEqualTo("PERMISO_CREATE");
        assertThat(resultado.pageNumber()).isZero();
        assertThat(resultado.pageSize()).isEqualTo(10);
        assertThat(resultado.totalElements()).isEqualTo(1);
        assertThat(resultado.totalPages()).isEqualTo(1);
        assertThat(resultado.isLast()).isTrue();

        verify(permisoRepositoryPort, times(1)).findAll(any(PermisoQuery.class));
    }

    @Test
    @DisplayName("Debería retornar una página vacía cuando no existen registros de permisos")
    void deberiaRetornarPaginaVacia_CuandoNoExistenPermisos() {
        // Arrange
        var query = new PermisoQuery();
        var paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        doReturn(paginaVacia).when(permisoRepositoryPort).findAll(any(PermisoQuery.class));

        // Act
        var resultado = permisoGetAllUseCase.getAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isEmpty();
        assertThat(resultado.totalElements()).isZero();
        assertThat(resultado.totalPages()).isZero();

        verify(permisoRepositoryPort, times(1)).findAll(any(PermisoQuery.class));
    }
}