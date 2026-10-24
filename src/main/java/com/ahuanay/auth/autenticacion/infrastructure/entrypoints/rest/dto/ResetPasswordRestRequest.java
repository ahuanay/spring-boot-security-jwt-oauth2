package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.constraint.ContrasenaSegura;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRestRequest(
    @Schema(description = "Token de recuperación incluido en el link del email", example = "3f9a1c...")
    @NotBlank(message = "El token de recuperación es obligatorio")
    String token,

    @Schema(description = "Nueva contraseña segura: entre 8 y 72 caracteres, con al menos una letra mayúscula, "
        + "una letra minúscula, un número y un símbolo especial", example = "NewSecurePassword123*")
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @ContrasenaSegura
    String newPassword
) {
}
