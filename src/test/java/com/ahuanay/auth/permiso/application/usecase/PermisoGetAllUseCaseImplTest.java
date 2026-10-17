package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoGetAllUseCaseImplTest {

    @Mock
    private PermisoRepositoryPort permisoRepositoryPort;

    @InjectMocks
    private PermisoGetAllUseCaseImpl permisoGetAllUseCase;

    private PermisoQuery query;
    private Permiso permisoDomain;
    private Page<Permiso> permisoPage;

    @BeforeEach
    void setUp() {
        query = new PermisoQuery();
        permisoDomain = new Permiso(1L, "PERMISO_CREATE");
        permisoPage = new PageImpl<>(List.of(permisoDomain), PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("Debería retornar una página de permisos exitosamente")
    void deberiaRetornarPaginaDePermisosExitosamente() {
        // Given
        when(permisoRepositoryPort.findAll(query)).thenReturn(permisoPage);

        // When
        PageResponse<PermisoResponse> resultado = permisoGetAllUseCase.getAll(query);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.content().get(0).id()).isEqualTo(1L);
        assertThat(resultado.content().get(0).nombre()).isEqualTo("PERMISO_CREATE");
        assertThat(resultado.pageNumber()).isEqualTo(0);
        assertThat(resultado.pageSize()).isEqualTo(10);
        assertThat(resultado.totalElements()).isEqualTo(1);
        assertThat(resultado.totalPages()).isEqualTo(1);
        assertThat(resultado.last()).isTrue();

        verify(permisoRepositoryPort, times(1)).findAll(query);
    }

    @Test
    @DisplayName("Debería retornar una página vacía cuando no existen registros de permisos")
    void deberiaRetornarPaginaVaciaCuandoNoExistenPermisos() {
        // Given
        Page<Permiso> paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(permisoRepositoryPort.findAll(query)).thenReturn(paginaVacia);

        // When
        PageResponse<PermisoResponse> resultado = permisoGetAllUseCase.getAll(query);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isEmpty();
        assertThat(resultado.totalElements()).isEqualTo(0);
        assertThat(resultado.totalPages()).isEqualTo(0);

        verify(permisoRepositoryPort, times(1)).findAll(query);
    }
}