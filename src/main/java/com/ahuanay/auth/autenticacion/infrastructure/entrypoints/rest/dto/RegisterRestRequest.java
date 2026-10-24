package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.constraint.ContrasenaSegura;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RegisterRestRequest(
    @Schema(description = "Correo electrónico del nuevo usuario", example = "paciente@email.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @Schema(description = "Contraseña segura: entre 8 y 72 caracteres, con al menos una letra mayúscula, "
        + "una letra minúscula, un número y un símbolo especial", example = "SecurePassword123*")
    @NotBlank(message = "La contraseña es obligatoria")
    @ContrasenaSegura
    String password,

    @Schema(description = "IDs de los roles a asignar", example = "[3]")
    @NotEmpty(message = "Debe asignar al menos un rol")
    Set<Long> roleIds
) {
}