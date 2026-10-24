package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRestRequest(
    @Schema(description = "Token de actualización (Refresh Token)", example = "eyJhbGciOiJIUzI1Ni...")
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {
}
