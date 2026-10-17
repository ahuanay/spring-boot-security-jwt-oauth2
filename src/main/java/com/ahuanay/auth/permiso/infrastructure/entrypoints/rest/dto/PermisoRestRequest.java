package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record PermisoRestRequest(
        @NotBlank(message = "El nombre del permiso es obligatorio")
        String nombre
) {}