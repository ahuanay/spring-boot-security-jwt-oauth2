package com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Schema(description = "Estructura estándar para la respuesta de errores de la API")
public record ErrorResponse(

    @Schema(description = "Marca de tiempo exacta en que ocurrió el error", example = "2026-10-13T17:11:00")
    LocalDateTime timestamp,

    @Schema(description = "Código de estado HTTP del error", example = "400")
    int status,

    @Schema(description = "Tipo o descripción corta del error HTTP", example = "Bad Request")
    String error,

    @Schema(description = "Mensaje general explicando la causa del error", example = "Error en la validación de los datos enviados.")
    String message,

    @Schema(description = "Errores específicos de validación por cada campo del request", example = "{\"nombre\": \"El nombre es obligatorio\"}")
    Map<String, String> errors,

    @Schema(description = "Lista de IDs que no fueron encontrados en la base de datos", example = "[100, 101]")
    Set<Long> notFoundIds
) {
}