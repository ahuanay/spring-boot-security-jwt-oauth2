package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.RolPermisoEntity;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.RolPermisoEntity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRolPermisoRepository extends JpaRepository<RolPermisoEntity, RolPermisoId> {
    List<RolPermisoEntity> findByPermisoId(Long permisoId);
}
