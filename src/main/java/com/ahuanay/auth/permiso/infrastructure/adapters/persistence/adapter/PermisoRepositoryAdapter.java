package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.mapper.PermisoMapper;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository.SpringDataPermisoRepository;
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

    private final SpringDataPermisoRepository permissionRepository;
    private final PermisoMapper permisoMapper;

    @Override
    public Page<Permiso> findAll(PermisoQuery query) {
        Sort.Direction direction = Sort.Direction.fromString(query.getDirection());
        Sort sort = Sort.by(direction, query.getSortBy());

        Pageable pageable = (query.getSize() == -1)
                ? Pageable.unpaged(sort)
                : PageRequest.of(query.getPage(), query.getSize(), sort);

        Specification<PermisoEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getNombre() != null && !query.getNombre().isBlank()) {
                predicates.add(cb.like(root.get("nombre"), "%" + query.getNombre() + "%"));
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