package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);
}