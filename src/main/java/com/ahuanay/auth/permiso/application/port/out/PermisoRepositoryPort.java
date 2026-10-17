package com.ahuanay.auth.permiso.application.port.out;

import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermisoRepositoryPort {
    Page<Permiso> findAll(PermisoQuery query);
    Optional<Permiso> findById(Long id);
    List<Permiso> findAllByIdIn(Set<Long> ids);
    Optional<Permiso> findByNombre(String nombre);
    Permiso save(Permiso permiso);
    void deleteById(Long id);
}