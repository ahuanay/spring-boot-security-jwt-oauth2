package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Estructura de respuesta para los datos de un permiso")
public record PermisoRestResponse(

    @Schema(description = "Identificador único del permiso", example = "1")
    Long id,

    @Schema(description = "Nombre único del permiso", example = "READ_USERS")
    String nombre
) {
}