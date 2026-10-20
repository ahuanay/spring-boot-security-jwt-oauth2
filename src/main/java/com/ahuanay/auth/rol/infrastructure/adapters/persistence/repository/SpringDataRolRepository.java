package com.ahuanay.auth.rol.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SpringDataRolRepository extends JpaRepository<RolEntity, Long>, JpaSpecificationExecutor<RolEntity> {
    Optional<RolEntity> findByNombre(String nombre);
}