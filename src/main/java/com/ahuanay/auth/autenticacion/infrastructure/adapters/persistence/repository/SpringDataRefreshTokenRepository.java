package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByToken(String token);

    void deleteByEmail(String email);
}