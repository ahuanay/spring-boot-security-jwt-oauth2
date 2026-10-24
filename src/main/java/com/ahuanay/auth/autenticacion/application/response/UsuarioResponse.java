package com.ahuanay.auth.autenticacion.application.response;

import com.ahuanay.auth.rol.application.response.RolResponse;

import java.util.Set;

public record UsuarioResponse(
    Long id,
    String email,
    Boolean active,
    Boolean mfaEnabled,
    Set<RolResponse> roles
) {
}