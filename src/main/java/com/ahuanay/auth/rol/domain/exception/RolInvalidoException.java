package com.ahuanay.auth.rol.domain.exception;

import com.ahuanay.auth.common.domain.exception.ReglaNegocioException;

public class RolInvalidoException extends ReglaNegocioException {
    public RolInvalidoException(String message) {
        super(message);
    }
}
