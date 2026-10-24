package com.ahuanay.auth.autenticacion.application.port.out;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;

import java.util.Optional;

public interface MfaOtpRepositoryPort {
    Optional<MfaOtp> findByTokenHash(String tokenHash);

    Optional<MfaOtp> findByEmailAndProposito(String email, PropositoOtp proposito);

    MfaOtp save(MfaOtp mfaOtp);

    void deleteByTokenHash(String tokenHash);

    void deleteByEmail(String email);

    void deleteByEmailAndProposito(String email, PropositoOtp proposito);
}
