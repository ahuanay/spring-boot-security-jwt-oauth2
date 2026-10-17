package com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Estructura estándar para la respuesta de errores de la API")
public record ErrorResponse(

    @Schema(description = "Marca de tiempo exacta en que ocurrió el error", example = "2026-10-13T17:11:00")
    LocalDateTime timestamp,

    @Schema(description = "Código de estado HTTP del error", example = "404")
    int status,

    @Schema(description = "Tipo o descripción corta del error HTTP", example = "Not Found")
    String error,

    @Schema(description = "Mensaje detallado explicando la causa del error", example = "No existe el permiso con ID: 5")
    String message
) {
}