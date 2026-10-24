package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRestRequest(
    @Schema(description = "Correo electrónico del usuario", example = "admin@mail.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @Schema(description = "Contraseña de acceso", example = "SecurePassword123*")
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {
}