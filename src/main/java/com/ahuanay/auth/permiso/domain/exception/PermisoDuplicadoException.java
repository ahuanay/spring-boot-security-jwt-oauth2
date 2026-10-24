package com.ahuanay.auth.permiso.domain.exception;

import com.ahuanay.auth.common.domain.exception.RecursoDuplicadoException;

public class PermisoDuplicadoException extends RecursoDuplicadoException {
    public PermisoDuplicadoException(String message) {
        super(message);
    }
}
