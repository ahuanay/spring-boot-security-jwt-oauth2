package com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class BaseExceptionHandler {

    protected final ZoneId zoneId;

    protected BaseExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        this.zoneId = ZoneId.of(timezone);
    }

    protected ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String title, String detail) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(zoneId),
                status.value(),
                title,
                detail
        );
        return ResponseEntity.status(status).body(error);
    }
}