package com.ahuanay.auth.rol.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.domain.model.Rol;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.mapper.RolMapper;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.repository.SpringDataRolRepository;
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
class RolRepositoryAdapterTest {

    @Mock
    private SpringDataRolRepository rolRepository;

    @Mock
    private RolMapper rolMapper;

    @InjectMocks
    private RolRepositoryAdapter rolRepositoryAdapter;

    private Rol rolDomain;
    private RolEntity rolEntity;

    @BeforeEach
    void setUp() {
        rolDomain = new Rol(1L, "DOCTOR", Set.of(new Permiso(1L, "PERMISO_CREATE")));
        rolEntity = new RolEntity(1L, "DOCTOR", Set.of(new PermisoEntity(1L, "PERMISO_CREATE")));
    }

    @Test
    @DisplayName("Debería retornar una página de roles cuando se aplica un filtro válido")
    void deberiaRetornarPaginaDeRoles_CuandoSeAplicaFiltroValido() {
        // Arrange
        RolQuery query = new RolQuery();
        query.setNombre("DOC");
        query.setPage(0);
        query.setSize(10);
        query.setSortBy("id");
        query.setDirection("ASC");

        Page<RolEntity> entityPage = new PageImpl<>(List.of(rolEntity));

        when(rolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(rolMapper.toDomain(rolEntity)).thenReturn(rolDomain);

        // Act
        Page<Rol> resultado = rolRepositoryAdapter.findAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(resultado.getContent().getFirst().getNombre()).isEqualTo("DOCTOR");

        verify(rolRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(rolMapper, times(1)).toDomain(rolEntity);
    }

    @Test
    @DisplayName("Debería consultar sin paginación cuando el tamaño de página es -1")
    void deberiaConsultarSinPaginacion_CuandoElTamanoDePaginaEsMenosUno() {
        // Arrange
        RolQuery query = new RolQuery();
        query.setSize(-1);
        Page<RolEntity> entityPage = new PageImpl<>(List.of(rolEntity));

        when(rolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(rolMapper.toDomain(rolEntity)).thenReturn(rolDomain);

        // Act
        Page<Rol> resultado = rolRepositoryAdapter.findAll(query);

        // Assert
        assertThat(resultado).isNotNull();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(rolRepository, times(1)).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().isUnpaged()).isTrue();
    }

    @Test
    @DisplayName("Debería retornar el rol cuando se busca por un ID existente")
    void deberiaRetornarRol_CuandoSeBuscaPorIdExistente() {
        // Arrange
        Long id = 1L;
        when(rolRepository.findById(id)).thenReturn(Optional.of(rolEntity));
        when(rolMapper.toDomain(rolEntity)).thenReturn(rolDomain);

        // Act
        Optional<Rol> resultado = rolRepositoryAdapter.findById(id);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(id);
        assertThat(resultado.get().getNombre()).isEqualTo("DOCTOR");

        verify(rolRepository, times(1)).findById(id);
        verify(rolMapper, times(1)).toDomain(rolEntity);
    }

    @Test
    @DisplayName("Debería retornar el rol cuando se busca por un nombre existente")
    void deberiaRetornarRol_CuandoSeBuscaPorNombreExistente() {
        // Arrange
        String nombre = "DOCTOR";
        when(rolRepository.findByNombre(nombre)).thenReturn(Optional.of(rolEntity));
        when(rolMapper.toDomain(rolEntity)).thenReturn(rolDomain);

        // Act
        Optional<Rol> resultado = rolRepositoryAdapter.findByNombre(nombre);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo(nombre);

        verify(rolRepository, times(1)).findByNombre(nombre);
        verify(rolMapper, times(1)).toDomain(rolEntity);
    }

    @Test
    @DisplayName("Debería retornar el rol guardado cuando los datos son válidos")
    void deberiaRetornarRolGuardado_CuandoDatosSonValidos() {
        // Arrange
        when(rolMapper.toEntity(rolDomain)).thenReturn(rolEntity);
        when(rolRepository.save(rolEntity)).thenReturn(rolEntity);
        when(rolMapper.toDomain(rolEntity)).thenReturn(rolDomain);

        // Act
        Rol resultado = rolRepositoryAdapter.save(rolDomain);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("DOCTOR");

        verify(rolMapper, times(1)).toEntity(rolDomain);
        verify(rolRepository, times(1)).save(rolEntity);
        verify(rolMapper, times(1)).toDomain(rolEntity);
    }

    @Test
    @DisplayName("Debería eliminar el rol exitosamente cuando se proporciona un ID válido")
    void deberiaEliminarRol_CuandoSeProporcionaIdValido() {
        // Arrange
        Long id = 1L;
        doNothing().when(rolRepository).deleteById(id);

        // Act
        rolRepositoryAdapter.deleteById(id);

        // Assert
        verify(rolRepository, times(1)).deleteById(id);
    }
}
