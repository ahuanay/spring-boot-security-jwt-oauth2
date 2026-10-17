package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception.BaseExceptionHandler;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.domain.exception.PermisoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ahuanay.auth.permiso")
@Order(1)
public class PermisoExceptionHandler extends BaseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermisoExceptionHandler.class);

    public PermisoExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        super(timezone);
    }

    @ExceptionHandler(PermisoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handlePermisoDuplicado(PermisoDuplicadoException ex) {
        LOGGER.warn("Duplicate permission error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(PermisoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handlePermisoInvalido(PermisoInvalidoException ex) {
        LOGGER.warn("Invalid permission error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }
}