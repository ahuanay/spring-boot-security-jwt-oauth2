package com.ahuanay.auth.rol.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.domain.model.Rol;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.mapper.RolMapper;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.repository.SpringDataRolRepository;
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

@Repository
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepositoryPort {

    private final SpringDataRolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    public Page<Rol> findAll(RolQuery query) {
        Sort.Direction direction = Sort.Direction.fromString(query.getDirection());
        Sort sort = Sort.by(direction, query.getSortBy());

        Pageable pageable = (query.getSize() == -1)
            ? Pageable.unpaged(sort)
            : PageRequest.of(query.getPage(), query.getSize(), sort);

        Specification<RolEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getNombre() != null && !query.getNombre().isBlank()) {
                predicates.add(cb.like(root.get("nombre"), "%" + query.getNombre() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return rolRepository.findAll(spec, pageable).map(rolMapper::toDomain);
    }

    @Override
    public Optional<Rol> findById(Long id) {
        return rolRepository.findById(id).map(rolMapper::toDomain);
    }

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre).map(rolMapper::toDomain);
    }

    @Override
    public Rol save(Rol rol) {
        RolEntity entity = rolMapper.toEntity(rol);
        RolEntity saved = rolRepository.save(entity);
        return rolMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        rolRepository.deleteById(id);
    }
}