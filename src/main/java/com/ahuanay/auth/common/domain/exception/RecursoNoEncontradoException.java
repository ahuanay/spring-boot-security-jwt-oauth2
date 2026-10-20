package com.ahuanay.auth.common.domain.exception;

import lombok.Getter;

import java.util.Set;

@Getter
public class RecursoNoEncontradoException extends RuntimeException {
    private final Set<Long> notFoundIds;

    public RecursoNoEncontradoException(String message) {
        super(message);
        this.notFoundIds = null;
    }

    public RecursoNoEncontradoException(String message, Long id) {
        super(message);
        this.notFoundIds = id != null ? Set.of(id) : null;
    }

    public RecursoNoEncontradoException(String message, Set<Long> notFoundIds) {
        super(message);
        this.notFoundIds = notFoundIds;
    }
}