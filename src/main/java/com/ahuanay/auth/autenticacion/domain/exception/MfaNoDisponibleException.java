package com.ahuanay.auth.autenticacion.domain.exception;

public class MfaNoDisponibleException extends RuntimeException {
    public MfaNoDisponibleException(String message) {
        super(message);
    }
}
