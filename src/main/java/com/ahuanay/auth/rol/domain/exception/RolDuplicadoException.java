package com.ahuanay.auth.rol.domain.exception;

import com.ahuanay.auth.common.domain.exception.RecursoDuplicadoException;

public class RolDuplicadoException extends RecursoDuplicadoException {
    public RolDuplicadoException(String message) {
        super(message);
    }
}
