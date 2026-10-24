package com.ahuanay.auth.permiso.domain.exception;

import com.ahuanay.auth.common.domain.exception.ReglaNegocioException;

public class PermisoInvalidoException extends ReglaNegocioException {
    public PermisoInvalidoException(String message) {
        super(message);
    }
}
