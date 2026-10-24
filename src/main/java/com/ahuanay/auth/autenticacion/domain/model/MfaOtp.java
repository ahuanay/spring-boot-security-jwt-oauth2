package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MfaOtp {

    public static final int LONGITUD_CODIGO = 6;

    private final Long id;
    private final String tokenHash;
    private final String email;
    private final String codigoHash;
    private final PropositoOtp proposito;
    private final LocalDateTime expiresAt;
    private int intentos;
    private LocalDateTime usedAt;

    public MfaOtp(
        Long id,
        String tokenHash,
        String email,
        String codigoHash,
        PropositoOtp proposito,
        LocalDateTime expiresAt,
        int intentos,
        LocalDateTime usedAt
    ) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new CredencialInvalidoException("El hash del token de doble factor no puede estar vacío.");
        }
        if (codigoHash == null || codigoHash.isBlank()) {
            throw new CredencialInvalidoException("El hash del código OTP no puede estar vacío.");
        }
        if (email == null || email.isBlank()) {
            throw new CredencialInvalidoException("El email asociado al código OTP no puede estar vacío.");
        }
        if (expiresAt == null) {
            throw new CredencialInvalidoException("La expiración del código OTP no puede ser nula.");
        }

        this.id = id;
        this.tokenHash = tokenHash;
        this.email = email;
        this.codigoHash = codigoHash;
        this.proposito = (proposito == null) ? PropositoOtp.LOGIN : proposito;
        this.expiresAt = expiresAt;
        this.intentos = Math.max(intentos, 0);
        this.usedAt = usedAt;
    }

    public boolean estaUsado() {
        return usedAt != null;
    }

    public boolean estaExpirado(LocalDateTime ahora) {
        return expiresAt.isBefore(ahora);
    }
    
    public void registrarIntentoFallido() {
        intentos++;
    }

    public void marcarComoUsado(LocalDateTime ahora) {
        if (usedAt == null) {
            this.usedAt = ahora;
        }
    }
}
