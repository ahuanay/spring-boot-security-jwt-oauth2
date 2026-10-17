package com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Estructura estándar para respuestas paginadas en la API")
public record PageRestResponse<T>(

        @Schema(description = "Lista de elementos de la página actual", requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> content,

        @Schema(description = "Número de la página actual (inicia en 0)", example = "0")
        int pageNumber,

        @Schema(description = "Cantidad de elementos por página", example = "10")
        int pageSize,

        @Schema(description = "Cantidad total de elementos en la base de datos", example = "55")
        long totalElements,

        @Schema(description = "Cantidad total de páginas disponibles", example = "6")
        int totalPages,

        @Schema(description = "Indica si esta es la última página del resultado", example = "false")
        boolean last
) {}