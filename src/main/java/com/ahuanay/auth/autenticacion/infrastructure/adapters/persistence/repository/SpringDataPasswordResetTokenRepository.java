package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteByEmail(String email);
}
