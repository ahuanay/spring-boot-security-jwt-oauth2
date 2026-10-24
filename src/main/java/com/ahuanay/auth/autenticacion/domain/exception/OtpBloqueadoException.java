package com.ahuanay.auth.autenticacion.domain.exception;

public class OtpBloqueadoException extends RuntimeException {
    public OtpBloqueadoException(String message) {
        super(message);
    }
}
