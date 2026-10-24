package com.ahuanay.auth.common.domain.exception;

public abstract class ReglaNegocioException extends RuntimeException {

    protected ReglaNegocioException(String message) {
        super(message);
    }
}
