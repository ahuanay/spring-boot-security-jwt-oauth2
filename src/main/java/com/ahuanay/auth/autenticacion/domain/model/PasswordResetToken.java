package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PasswordResetToken {

    private final Long id;
    private final String tokenHash;
    private final String email;
    private final LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public PasswordResetToken(Long id, String tokenHash, String email, LocalDateTime expiresAt, LocalDateTime usedAt) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new CredencialInvalidoException("El hash del token de recuperación no puede estar vacío.");
        }
        if (email == null || email.isBlank()) {
            throw new CredencialInvalidoException("El email asociado al token de recuperación no puede estar vacío.");
        }
        if (expiresAt == null) {
            throw new CredencialInvalidoException("La expiración del token de recuperación no puede ser nula.");
        }

        this.id = id;
        this.tokenHash = tokenHash;
        this.email = email;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public boolean estaUsado() {
        return usedAt != null;
    }

    public boolean estaExpirado(LocalDateTime ahora) {
        return expiresAt.isBefore(ahora);
    }

    public void marcarComoUsado(LocalDateTime ahora) {
        if (usedAt == null) {
            this.usedAt = ahora;
        }
    }
}
