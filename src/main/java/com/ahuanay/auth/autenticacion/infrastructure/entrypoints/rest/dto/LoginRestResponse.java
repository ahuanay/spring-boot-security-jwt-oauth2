package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Respuesta del inicio de sesión (directa o con desafío de doble factor)")
public record LoginRestResponse(

    @Schema(description = "Indica si el usuario debe verificar el código OTP por email", example = "true")
    Boolean mfaRequerido,

    @Schema(description = "Token de acceso de corta duración (null si mfaRequerido=true)", example = "eyJhbGciOiJIUzI1Ni...")
    String accessToken,

    @Schema(description = "Token para refrescar el acceso (null si mfaRequerido=true)", example = "eyJhbGciOiJIUzI1Ni...")
    String refreshToken,

    @Schema(description = "Tipo de esquema del token (null si mfaRequerido=true)", example = "Bearer")
    String tokenType,

    @Schema(description = "Token intermedio para verificar el código OTP (null si mfaRequerido=false)",
        example = "b74db3d7d1b0f4a9cae56b5c9b0e1c4d9b2f8a6e4c8d1b3f7a5e9c2d4f6b8a0e")
    String tokenMfa,

    @Schema(description = "Segundos de validez del código OTP (null si mfaRequerido=false)", example = "300")
    Long expiraEnSegundos
) {
}
