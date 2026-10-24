package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRestRequest(
    @Schema(description = "Correo electrónico del usuario local que solicita la recuperación", example = "paciente@email.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email
) {
}
