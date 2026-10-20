package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.mapper.PermisoMapper;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataPermisoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoRepositoryAdapterTest {

    @Mock
    private SpringDataPermisoRepository permissionRepository;

    @Mock
    private PermisoMapper permisoMapper;

    @InjectMocks
    private PermisoRepositoryAdapter permisoRepositoryAdapter;

    private Permiso permisoDomain;
    private PermisoEntity permisoEntity;

    @BeforeEach
    void setUp() {
        permisoDomain = new Permiso(1L, "PERMISO_CREATE");
        permisoEntity = new PermisoEntity(1L, "PERMISO_CREATE");
    }

    @Test
    @DisplayName("Debería retornar una página de permisos cuando se aplica un filtro válido")
    void deberiaRetornarPaginaDePermisos_CuandoSeAplicaFiltroValido() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setNombre("CREATE");
        query.setPage(0);
        query.setSize(10);
        query.setSortBy("id");
        query.setDirection("ASC");

        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));

        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Page<Permiso> resultado = permisoRepositoryAdapter.findAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(resultado.getContent().getFirst().getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permissionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería consultar sin paginación cuando el tamaño de página es -1")
    void deberiaConsultarSinPaginacion_CuandoElTamanoDePaginaEsMenosUno() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setSize(-1);
        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));

        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Page<Permiso> resultado = permisoRepositoryAdapter.findAll(query);

        // Assert
        assertThat(resultado).isNotNull();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(permissionRepository, times(1)).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().isUnpaged()).isTrue();
    }

    @Test
    @DisplayName("Debería retornar el permiso cuando se busca por un ID existente")
    void deberiaRetornarPermiso_CuandoSeBuscaPorIdExistente() {
        // Arrange
        Long id = 1L;
        when(permissionRepository.findById(id)).thenReturn(Optional.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Optional<Permiso> resultado = permisoRepositoryAdapter.findById(id);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(id);
        assertThat(resultado.get().getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permissionRepository, times(1)).findById(id);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería retornar una lista de permisos cuando se buscan por una lista de IDs válidos")
    void deberiaRetornarListaDePermisos_CuandoSeBuscanPorIdsValidos() {
        // Arrange
        Set<Long> ids = Set.of(1L);
        when(permissionRepository.findAllById(ids)).thenReturn(List.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        List<Permiso> resultado = permisoRepositoryAdapter.findAllByIdIn(ids);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(1L);

        verify(permissionRepository, times(1)).findAllById(ids);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería retornar el permiso cuando se busca por un nombre existente")
    void deberiaRetornarPermiso_CuandoSeBuscaPorNombreExistente() {
        // Arrange
        String nombre = "PERMISO_CREATE";
        when(permissionRepository.findByNombre(nombre)).thenReturn(Optional.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Optional<Permiso> resultado = permisoRepositoryAdapter.findByNombre(nombre);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo(nombre);

        verify(permissionRepository, times(1)).findByNombre(nombre);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería retornar el permiso guardado cuando los datos son válidos")
    void deberiaRetornarPermisoGuardado_CuandoDatosSonValidos() {
        // Arrange
        when(permisoMapper.toEntity(permisoDomain)).thenReturn(permisoEntity);
        when(permissionRepository.save(permisoEntity)).thenReturn(permisoEntity);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Permiso resultado = permisoRepositoryAdapter.save(permisoDomain);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permisoMapper, times(1)).toEntity(permisoDomain);
        verify(permissionRepository, times(1)).save(permisoEntity);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería eliminar el permiso exitosamente cuando se proporciona un ID válido")
    void deberiaEliminarPermiso_CuandoSeProporcionaIdValido() {
        // Arrange
        Long id = 1L;
        doNothing().when(permissionRepository).deleteById(id);

        // Act
        permisoRepositoryAdapter.deleteById(id);

        // Assert
        verify(permissionRepository, times(1)).deleteById(id);
    }
}