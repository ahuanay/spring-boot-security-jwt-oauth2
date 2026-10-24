package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.MfaNoDisponibleException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpInvalidoException;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.ahuanay.auth.autenticacion")
@Order(1)
public class AutenticacionExceptionHandler extends BaseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacionExceptionHandler.class);

    public AutenticacionExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        super(timezone);
    }

    @ExceptionHandler(CredencialInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCredencialInvalido(CredencialInvalidoException ex) {
        LOGGER.warn("Invalid credentials error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(OtpInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleOtpInvalido(OtpInvalidoException ex) {
        LOGGER.warn("Invalid OTP error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(OtpBloqueadoException.class)
    public ResponseEntity<ErrorResponse> handleOtpBloqueado(OtpBloqueadoException ex) {
        LOGGER.warn("OTP attempts exhausted error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage());
    }

    @ExceptionHandler(MfaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleMfaNoDisponible(MfaNoDisponibleException ex) {
        LOGGER.warn("MFA not available error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }
}
