package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.MfaOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMfaOtpRepository extends JpaRepository<MfaOtpEntity, Long> {
    Optional<MfaOtpEntity> findByTokenHash(String tokenHash);

    Optional<MfaOtpEntity> findByEmailAndProposito(String email, PropositoOtp proposito);

    void deleteByTokenHash(String tokenHash);

    void deleteByEmail(String email);

    void deleteByEmailAndProposito(String email, PropositoOtp proposito);
}
