package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SpringDataPermisoRepository extends JpaRepository<PermisoEntity, Long>, JpaSpecificationExecutor<PermisoEntity> {
    Optional<PermisoEntity> findByNombre(String nombre);
}