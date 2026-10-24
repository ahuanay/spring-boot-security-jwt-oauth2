package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Solicitud de reenvío del código OTP de doble factor")
public record ReenviarOtpRestRequest(

    @Schema(description = "Token intermedio devuelto por el login cuando mfaRequerido=true",
        example = "b74db3d7d1b0f4a9cae56b5c9b0e1c4d9b2f8a6e4c8d1b3f7a5e9c2d4f6b8a0e")
    @NotBlank(message = "El token de verificación es obligatorio.")
    String tokenMfa
) {
}
