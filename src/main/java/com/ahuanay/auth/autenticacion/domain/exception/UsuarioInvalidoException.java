package com.ahuanay.auth.autenticacion.domain.exception;

import com.ahuanay.auth.common.domain.exception.ReglaNegocioException;

public class UsuarioInvalidoException extends ReglaNegocioException {
    public UsuarioInvalidoException(String message) {
        super(message);
    }
}
