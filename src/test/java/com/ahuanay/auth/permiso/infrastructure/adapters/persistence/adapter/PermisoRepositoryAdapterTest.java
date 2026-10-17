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
    @DisplayName("Debería retornar una página de permisos usando el adaptador")
    void deberiaRetornarPaginaDePermisos() {
        // Given
        PermisoQuery query = new PermisoQuery();
        query.setNombre("CREATE");
        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));

        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // When
        Page<Permiso> resultado = permisoRepositoryAdapter.findAll(query);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(resultado.getContent().getFirst().getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permissionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería buscar permiso por ID exitosamente")
    void deberiaBuscarPermisoPorIdExitosamente() {
        // Given
        Long id = 1L;
        when(permissionRepository.findById(id)).thenReturn(Optional.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // When
        Optional<Permiso> resultado = permisoRepositoryAdapter.findById(id);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(id);
        assertThat(resultado.get().getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permissionRepository, times(1)).findById(id);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería buscar permisos por lista de IDs exitosamente")
    void deberiaBuscarPermisosPorIdsExitosamente() {
        // Given
        Set<Long> ids = Set.of(1L);
        when(permissionRepository.findAllById(ids)).thenReturn(List.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // When
        List<Permiso> resultado = permisoRepositoryAdapter.findAllByIdIn(ids);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(1L);

        verify(permissionRepository, times(1)).findAllById(ids);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería buscar permiso por nombre exitosamente")
    void deberiaBuscarPermisoPorNombreExitosamente() {
        // Given
        String nombre = "PERMISO_CREATE";
        when(permissionRepository.findByNombre(nombre)).thenReturn(Optional.of(permisoEntity));
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // When
        Optional<Permiso> resultado = permisoRepositoryAdapter.findByNombre(nombre);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo(nombre);

        verify(permissionRepository, times(1)).findByNombre(nombre);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería guardar un permiso exitosamente")
    void deberiaGuardarPermisoExitosamente() {
        // Given
        when(permisoMapper.toEntity(permisoDomain)).thenReturn(permisoEntity);
        when(permissionRepository.save(permisoEntity)).thenReturn(permisoEntity);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // When
        Permiso resultado = permisoRepositoryAdapter.save(permisoDomain);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("PERMISO_CREATE");

        verify(permisoMapper, times(1)).toEntity(permisoDomain);
        verify(permissionRepository, times(1)).save(permisoEntity);
        verify(permisoMapper, times(1)).toDomain(permisoEntity);
    }

    @Test
    @DisplayName("Debería eliminar un permiso por ID exitosamente")
    void deberiaEliminarPermisoPorIdExitosamente() {
        // Given
        Long id = 1L;
        doNothing().when(permissionRepository).deleteById(id);

        // When
        permisoRepositoryAdapter.deleteById(id);

        // Then
        verify(permissionRepository, times(1)).deleteById(id);
    }
}