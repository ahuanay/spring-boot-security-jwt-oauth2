package com.ahuanay.auth.rol.application.response;

import com.ahuanay.auth.permiso.application.response.PermisoResponse;

import java.util.Set;

public record RolResponse(
    Long id,
    String nombre,
    Set<PermisoResponse> permisos
) {
}