package com.ahuanay.auth.autenticacion.domain.exception;

import com.ahuanay.auth.common.domain.exception.RecursoDuplicadoException;

public class UsuarioDuplicadoException extends RecursoDuplicadoException {
    public UsuarioDuplicadoException(String message) {
        super(message);
    }
}
