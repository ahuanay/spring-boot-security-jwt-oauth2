package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

public final class JwtClaims {

    public static final String USUARIO_ID = "userId";

    public static final String ROLES = "roles";

    public static final String PERMISOS = "permisos";

    public static final String PREFIJO_ROL = "ROLE_";

    private JwtClaims() {
    }
}
