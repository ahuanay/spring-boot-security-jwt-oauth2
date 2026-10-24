package com.ahuanay.auth.common.domain.exception;

public abstract class RecursoDuplicadoException extends RuntimeException {

    protected RecursoDuplicadoException(String message) {
        super(message);
    }
}
