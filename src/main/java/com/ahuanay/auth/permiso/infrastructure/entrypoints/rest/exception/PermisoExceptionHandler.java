package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.domain.exception.PermisoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.ahuanay.auth.permiso")
@Order(1)
public class PermisoExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermisoExceptionHandler.class);

    @ExceptionHandler(PermisoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handlePermisoDuplicado(PermisoDuplicadoException ex) {
        LOGGER.warn("Duplicate permission error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PermisoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handlePermisoInvalido(PermisoInvalidoException ex) {
        LOGGER.warn("Invalid permission error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}