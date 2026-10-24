package com.ahuanay.auth.permiso.domain.exception;

import com.ahuanay.auth.common.domain.exception.RecursoDuplicadoException;

public class PermisoEnUsoException extends RecursoDuplicadoException {

    public PermisoEnUsoException(String message) {
        super(message);
    }
}
