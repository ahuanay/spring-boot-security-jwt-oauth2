package com.ahuanay.auth.rol.application.port.out;

import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.domain.model.Rol;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface RolRepositoryPort {
    Page<Rol> findAll(RolQuery query);

    Optional<Rol> findById(Long id);

    Optional<Rol> findByNombre(String name);

    Rol save(Rol rol);

    void deleteById(Long id);
}