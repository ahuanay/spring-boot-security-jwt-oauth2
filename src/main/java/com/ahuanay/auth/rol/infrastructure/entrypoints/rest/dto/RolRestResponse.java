package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto;

import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "Estructura de respuesta para los datos de un rol y sus permisos asociados")
public record RolRestResponse(

    @Schema(description = "Identificador único del rol", example = "1")
    Long id,

    @Schema(description = "Nombre único del rol", example = "ROLE_ADMIN")
    String nombre,

    @Schema(description = "Conjunto de permisos asociados a este rol")
    Set<PermisoRestResponse> permisos
) {
}