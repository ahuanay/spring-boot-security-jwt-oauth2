package com.ahuanay.auth.autenticacion.application.port.out;

import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    PasswordResetToken save(PasswordResetToken token);

    void deleteByTokenHash(String tokenHash);

    void deleteByEmail(String email);
}
