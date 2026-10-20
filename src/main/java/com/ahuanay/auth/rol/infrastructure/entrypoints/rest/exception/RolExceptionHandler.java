package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception.BaseExceptionHandler;
import com.ahuanay.auth.rol.domain.exception.RolDuplicadoException;
import com.ahuanay.auth.rol.domain.exception.RolInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ahuanay.auth.rol")
@Order(1)
public class RolExceptionHandler extends BaseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RolExceptionHandler.class);

    public RolExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        super(timezone);
    }

    @ExceptionHandler(RolDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRolDuplicado(RolDuplicadoException ex) {
        LOGGER.warn("Duplicate role error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(RolInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleRolInvalido(RolInvalidoException ex) {
        LOGGER.warn("Invalid role error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }
}