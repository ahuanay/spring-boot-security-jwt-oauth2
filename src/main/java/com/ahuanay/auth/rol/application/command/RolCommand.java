package com.ahuanay.auth.rol.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RolCommand(
    @NotBlank(message = "El nombre del rol es obligatorio")
    String nombre,

    @NotEmpty(message = "El rol debe tener al menos un permiso asociado")
    Set<Long> permisoIds
) {
}