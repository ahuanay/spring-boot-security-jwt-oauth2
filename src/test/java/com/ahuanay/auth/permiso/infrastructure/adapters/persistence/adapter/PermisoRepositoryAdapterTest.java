package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.common.domain.exception.ParametroInvalidoException;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.RolPermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.mapper.PermisoMapper;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataPermisoRepository;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataRolPermisoRepository;
import jakarta.persistence.criteria.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoRepositoryAdapterTest {

    @Mock
    private SpringDataPermisoRepository permissionRepository;

    @Mock
    private SpringDataRolPermisoRepository rolPermisoRepository;

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
    @DisplayName("Debería lanzar ParametroInvalidoException cuando el sortBy no es un campo permitido")
    void deberiaLanzarParametroInvalidoException_CuandoElSortByNoEsPermitido() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setSortBy("password");

        // Act & Assert
        assertThatThrownBy(() -> permisoRepositoryAdapter.findAll(query))
            .isInstanceOf(ParametroInvalidoException.class)
            .hasMessage("El parámetro 'sortBy' con valor 'password' no es válido. Valores permitidos: id, nombre.");

        verify(permissionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debería lanzar ParametroInvalidoException cuando la dirección de ordenamiento no es válida")
    void deberiaLanzarParametroInvalidoException_CuandoLaDireccionNoEsValida() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setSortBy("nombre");
        query.setDirection("ASCENTANTE");

        // Act & Assert
        assertThatThrownBy(() -> permisoRepositoryAdapter.findAll(query))
            .isInstanceOf(ParametroInvalidoException.class)
            .hasMessage("El parámetro 'direction' con valor 'ASCENTANTE' no es válido. Valores permitidos: ASC, DESC.");

        verify(permissionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debería aceptar sortBy y direction en cualquier capitalización")
    void deberiaAceptarSortByYDirection_EnCualquierCapitalizacion() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setSortBy("NOMBRE");
        query.setDirection("desc");
        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));

        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        Page<Permiso> resultado = permisoRepositoryAdapter.findAll(query);

        // Assert
        assertThat(resultado).isNotNull();
        verify(permissionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debería retornar los IDs de roles que usan el permiso cuando se consulta el uso del permiso")
    void deberiaRetornarIdsDeRoles_CuandoSeConsultaElUsoDelPermiso() {
        // Arrange
        Long permisoId = 1L;
        RolPermisoEntity asignacion1 = new RolPermisoEntity(1L, permisoId);
        RolPermisoEntity asignacion2 = new RolPermisoEntity(2L, permisoId);
        when(rolPermisoRepository.findByPermisoId(permisoId)).thenReturn(List.of(asignacion1, asignacion2));

        // Act
        List<Long> rolIds = permisoRepositoryAdapter.findRolIdsByPermisoId(permisoId);

        // Assert
        assertThat(rolIds).containsExactly(1L, 2L);
        verify(rolPermisoRepository, times(1)).findByPermisoId(permisoId);
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

    @Test
    @DisplayName("Debería construir el predicate de filtro por nombre cuando se consulta con filtro")
    @SuppressWarnings("unchecked")
    void deberiaConstruirPredicateDeFiltroPorNombre_CuandoSeConsultaConFiltro() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setNombre("per%mi_so");
        query.setSortBy("id");
        query.setDirection("ASC");

        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));
        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        permisoRepositoryAdapter.findAll(query);

        // Assert
        ArgumentCaptor<Specification<PermisoEntity>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(permissionRepository).findAll(specificationCaptor.capture(), any(Pageable.class));

        Root<PermisoEntity> root = mock(Root.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Expression<String> expression = mock(Expression.class);
        Path<String> path = mock(Path.class);
        Predicate likePredicate = mock(Predicate.class);
        Predicate predicateResultado = mock(Predicate.class);

        doReturn(path).when(root).get("nombre");
        when(criteriaBuilder.lower(path)).thenReturn(expression);
        when(criteriaBuilder.like(expression, "%per\\%mi\\_so%", '\\')).thenReturn(likePredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicateResultado);

        Predicate predicate = specificationCaptor.getValue().toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(predicate).isEqualTo(predicateResultado);
        verify(criteriaBuilder).like(expression, "%per\\%mi\\_so%", '\\');
        verifyNoMoreInteractions(expression);
    }

    @Test
    @DisplayName("Debería construir el predicate vacío cuando el filtro de nombre no está presente")
    @SuppressWarnings("unchecked")
    void deberiaConstruirPredicateVacio_CuandoElFiltroDeNombreNoEstaPresente() {
        // Arrange
        PermisoQuery query = new PermisoQuery();
        query.setSortBy("id");
        query.setDirection("ASC");

        Page<PermisoEntity> entityPage = new PageImpl<>(List.of(permisoEntity));
        when(permissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(permisoMapper.toDomain(permisoEntity)).thenReturn(permisoDomain);

        // Act
        permisoRepositoryAdapter.findAll(query);

        // Assert
        ArgumentCaptor<Specification<PermisoEntity>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(permissionRepository).findAll(specificationCaptor.capture(), any(Pageable.class));

        Root<PermisoEntity> root = mock(Root.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate predicateResultado = mock(Predicate.class);

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicateResultado);

        Predicate predicate = specificationCaptor.getValue().toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(predicate).isEqualTo(predicateResultado);
        verifyNoInteractions(root);
    }
}