package com.ahuanay.auth.support;

import com.ahuanay.auth.permiso.domain.model.Permiso;

public final class TestPermisos {

    public static Permiso permiso(Long id, String nombre) {
        return new Permiso(id, nombre);
    }

    public static Permiso lectura() {
        return permiso(1L, "PERMISO_READ");
    }
    
    public static Permiso creacion() {
        return permiso(1L, "PERMISO_CREATE");
    }

    private TestPermisos() {
    }
}
