package com.ahuanay.auth.autenticacion.application.port.out;

import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken refreshToken);

    void deleteByToken(String token);

    void deleteByEmail(String email);
}