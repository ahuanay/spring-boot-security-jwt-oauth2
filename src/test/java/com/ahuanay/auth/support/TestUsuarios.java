package com.ahuanay.auth.support;

import com.ahuanay.auth.autenticacion.domain.model.Usuario;

public final class TestUsuarios {

    public static Usuario local() {
        return base().build();
    }

    public static Usuario localConMfa() {
        return base().conMfa().build();
    }

    public static Usuario localInactivo() {
        return base().inactivo().build();
    }

    public static Usuario google() {
        return base().google().build();
    }

    public static Usuario github() {
        return base().github().build();
    }

    public static UsuarioBuilder base() {
        return new UsuarioBuilder();
    }

    private TestUsuarios() {
    }
}
