package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.common.infrastructure.utils.JpaQueryUtils;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.RolPermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.mapper.PermisoMapper;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataPermisoRepository;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataRolPermisoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PermisoRepositoryAdapter implements PermisoRepositoryPort {

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "nombre");

    private final SpringDataPermisoRepository permissionRepository;
    private final SpringDataRolPermisoRepository rolPermisoRepository;
    private final PermisoMapper permisoMapper;

    @Override
    public Page<Permiso> findAll(PermisoQuery query) {
        Sort sort = JpaQueryUtils.resolverSort(query.getSortBy(), query.getDirection(), CAMPOS_ORDENABLES);

        Pageable pageable = (query.getSize() == -1)
            ? Pageable.unpaged(sort)
            : PageRequest.of(query.getPage(), query.getSize(), sort);

        Specification<PermisoEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getNombre() != null && !query.getNombre().isBlank()) {
                String patron = "%" + JpaQueryUtils.escaparLike(query.getNombre().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("nombre")), patron, '\\'));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return permissionRepository.findAll(spec, pageable)
            .map(permisoMapper::toDomain);
    }

    @Override
    public Optional<Permiso> findById(Long id) {
        return permissionRepository.findById(id)
            .map(permisoMapper::toDomain);
    }

    @Override
    public List<Permiso> findAllByIdIn(Set<Long> ids) {
        return permissionRepository.findAllById(ids).stream()
            .map(permisoMapper::toDomain)
            .toList();
    }

    @Override
    public List<Long> findRolIdsByPermisoId(Long permisoId) {
        return rolPermisoRepository.findByPermisoId(permisoId).stream()
            .map(RolPermisoEntity::getRolId)
            .toList();
    }

    @Override
    public Optional<Permiso> findByNombre(String nombre) {
        return permissionRepository.findByNombre(nombre)
            .map(permisoMapper::toDomain);
    }

    @Override
    public Permiso save(Permiso permiso) {
        PermisoEntity entity = permisoMapper.toEntity(permiso);
        PermisoEntity saved = permissionRepository.save(entity);
        return permisoMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }
}