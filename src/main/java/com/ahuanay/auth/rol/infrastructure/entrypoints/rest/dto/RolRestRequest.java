package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.Set;

@Schema(description = "Estructura de solicitud para la creación o actualización de un rol")
public record RolRestRequest(

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Schema(description = "Nombre único del rol", example = "ROLE_ADMIN")
    String nombre,

    @NotEmpty(message = "Debe asociar al menos un ID de permiso al rol")
    @Schema(
        description = "Conjunto de identificadores numéricos de los permisos a asociar",
        example = "[1, 2, 3]"
    )
    Set<@Positive(message = "El ID del permiso debe ser un número entero positivo") Long> permisoIds
) {
}