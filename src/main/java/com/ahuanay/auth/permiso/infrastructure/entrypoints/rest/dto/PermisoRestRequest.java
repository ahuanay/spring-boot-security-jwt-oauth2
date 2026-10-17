package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Estructura de solicitud para la creación o actualización de un permiso")
public record PermisoRestRequest(

        @NotBlank(message = "El nombre del permiso es obligatorio")
        @Schema(
                description = "Nombre único que identifica el permiso en el sistema",
                example = "READ_USERS",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nombre
) {}