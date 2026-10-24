package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefreshToken {

    private final Long id;
    private final String token;
    private final String email;
    private final LocalDateTime expiresAt;

    public RefreshToken(Long id, String token, String email, LocalDateTime expiresAt) {
        if (token == null || token.isBlank()) {
            throw new CredencialInvalidoException("El token no puede estar vacío.");
        }
        if (email == null || email.isBlank()) {
            throw new CredencialInvalidoException("El email asociado al token no puede estar vacío.");
        }
        if (expiresAt == null) {
            throw new CredencialInvalidoException("La expiración del token no puede ser nula.");
        }

        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
    }
}
