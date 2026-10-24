package com.ahuanay.auth.support;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.domain.model.Rol;

import java.util.Set;

public final class TestRoles {

    public static Rol rol(Long id, String nombre, Permiso... permisos) {
        return new Rol(id, nombre, Set.of(permisos));
    }

    public static Rol paciente() {
        return rol(1L, "PACIENTE", TestPermisos.lectura());
    }

    public static Rol paciente(Long id) {
        return rol(id, "PACIENTE", TestPermisos.lectura());
    }

    public static Rol doctor() {
        return rol(1L, "DOCTOR", TestPermisos.creacion());
    }

    private TestRoles() {
    }
}
