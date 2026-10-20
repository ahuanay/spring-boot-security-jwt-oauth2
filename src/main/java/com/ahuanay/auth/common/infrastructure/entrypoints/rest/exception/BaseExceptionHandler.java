package com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

public abstract class BaseExceptionHandler {

    protected final ZoneId zoneId;

    protected BaseExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        this.zoneId = ZoneId.of(timezone);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String title, String message, Map<String, String> errors, Set<Long> notFoundIds) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(zoneId),
            status.value(),
            title,
            message,
            errors,
            notFoundIds
        );
        return ResponseEntity.status(status).body(error);
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponseWithErrors(HttpStatus status, String title, String message, Map<String, String> errors) {
        return buildResponse(status, title, message, errors, null);
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponseWithIds(HttpStatus status, String title, String message, Set<Long> notFoundIds) {
        return buildResponse(status, title, message, null, notFoundIds);
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String title, String message) {
        return buildResponse(status, title, message, null, null);
    }
}