package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.domain.model.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolGetAllUseCaseImplTest {

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @InjectMocks
    private RolGetAllUseCaseImpl rolGetAllUseCase;

    @Test
    @DisplayName("Debería retornar una página de roles exitosamente cuando existen roles")
    void deberiaRetornarPaginaDeRolesExitosamente_CuandoExistenRoles() {
        // Arrange
        var query = new RolQuery();
        var permisoDomain = new Permiso(1L, "PERMISO_CREATE");
        var rolDomain = new Rol(1L, "DOCTOR", Set.of(permisoDomain));
        var springPage = new PageImpl<>(List.of(rolDomain), PageRequest.of(0, 10), 1);

        doReturn(springPage).when(rolRepositoryPort).findAll(any(RolQuery.class));

        // Act
        var resultado = rolGetAllUseCase.getAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).hasSize(1);
        assertThat(resultado.content().getFirst().id()).isEqualTo(1L);
        assertThat(resultado.content().getFirst().nombre()).isEqualTo("DOCTOR");
        assertThat(resultado.content().getFirst().permisos()).hasSize(1);
        assertThat(resultado.content().getFirst().permisos().iterator().next().id()).isEqualTo(1L);
        assertThat(resultado.content().getFirst().permisos().iterator().next().nombre()).isEqualTo("PERMISO_CREATE");
        assertThat(resultado.pageNumber()).isZero();
        assertThat(resultado.pageSize()).isEqualTo(10);
        assertThat(resultado.totalElements()).isEqualTo(1);
        assertThat(resultado.totalPages()).isEqualTo(1);
        assertThat(resultado.last()).isTrue();

        verify(rolRepositoryPort, times(1)).findAll(any(RolQuery.class));
    }

    @Test
    @DisplayName("Debería retornar una página vacía cuando no existen registros de roles")
    void deberiaRetornarPaginaVacia_CuandoNoExistenRoles() {
        // Arrange
        var query = new RolQuery();
        var paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        doReturn(paginaVacia).when(rolRepositoryPort).findAll(any(RolQuery.class));

        // Act
        var resultado = rolGetAllUseCase.getAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.content()).isEmpty();
        assertThat(resultado.totalElements()).isZero();
        assertThat(resultado.totalPages()).isZero();

        verify(rolRepositoryPort, times(1)).findAll(any(RolQuery.class));
    }
}
