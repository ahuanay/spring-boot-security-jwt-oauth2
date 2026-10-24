package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Verificación del segundo factor de autenticación por OTP de email")
public record VerificarOtpRestRequest(

    @Schema(description = "Token intermedio devuelto por el login cuando mfaRequerido=true",
        example = "b74db3d7d1b0f4a9cae56b5c9b0e1c4d9b2f8a6e4c8d1b3f7a5e9c2d4f6b8a0e")
    @NotBlank(message = "El token de verificación es obligatorio.")
    String tokenMfa,

    @Schema(description = "Código OTP de 6 dígitos enviado al email del usuario", example = "482915")
    @NotNull(message = "El código OTP es obligatorio.")
    @Pattern(regexp = "\\d{6}", message = "El código OTP debe tener exactamente 6 dígitos.")
    String codigo
) {
}
