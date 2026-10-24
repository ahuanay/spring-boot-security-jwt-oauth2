package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Respuesta de autenticación con tokens JWT")
public record AuthRestResponse(

    @Schema(description = "Token de acceso de corta duración", example = "eyJhbGciOiJIUzI1Ni...")
    String accessToken,

    @Schema(description = "Token para refrescar el acceso", example = "eyJhbGciOiJIUzI1Ni...")
    String refreshToken,

    @Schema(description = "Tipo de esquema del token", example = "Bearer")
    String tokenType
) {
}