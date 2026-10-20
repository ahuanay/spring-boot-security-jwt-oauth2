package com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler(@Value("${app.timezone:America/Lima}") String timezone) {
        super(timezone);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.warn("Malformed JSON request or type mismatch: {}", ex.getMessage());

        String message = "El formato del cuerpo de la solicitud (JSON) es inválido.";

        if (ex.getCause() instanceof InvalidFormatException invalidEx) {
            String campo = invalidEx.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .findFirst()
                .orElse("desconocido");

            String tipoEsperado = invalidEx.getTargetType().getSimpleName();

            message = String.format(
                "El campo '%s' contiene un tipo de dato inválido. Se esperaba un valor de tipo %s.",
                campo,
                tipoEsperado
            );
        }

        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            message
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        LOGGER.warn("Validation error in request payload: {}", errors);

        return buildErrorResponseWithErrors(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Uno o más campos presentan errores de validación.",
            errors
        );
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        LOGGER.warn("Resource not found: {}", ex.getMessage());

        if (ex.getNotFoundIds() != null && !ex.getNotFoundIds().isEmpty()) {
            return buildErrorResponseWithIds(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                ex.getNotFoundIds()
            );
        }

        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage()
        );
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleGeneralException(Throwable ex) {
        LOGGER.error("An unexpected error occurred in the server: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocurrió un error inesperado en el servidor.");
    }
}