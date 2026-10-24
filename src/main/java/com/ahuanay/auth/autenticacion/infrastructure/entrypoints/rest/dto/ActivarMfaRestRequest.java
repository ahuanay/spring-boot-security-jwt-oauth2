package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Inicio de la activación del doble factor de autenticación (requiere confirmar la contraseña)")
public record ActivarMfaRestRequest(

    @Schema(description = "Contraseña actual del usuario para confirmar la operación", example = "SecurePassword123*")
    @NotBlank(message = "La contraseña es obligatoria.")
    String password
) {
}
