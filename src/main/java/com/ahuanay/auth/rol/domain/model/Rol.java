package com.ahuanay.auth.rol.domain.model;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.domain.exception.RolInvalidoException;
import lombok.Getter;

import java.util.Set;

@Getter
public class Rol {

    private final Long id;
    private final String nombre;
    private final Set<Permiso> permisos;

    public Rol(Long id, String nombre, Set<Permiso> permisos) {
        if (nombre == null || nombre.isBlank()) {
            throw new RolInvalidoException("El nombre del rol no puede estar vacío.");
        }
        if (permisos == null || permisos.isEmpty()) {
            throw new RolInvalidoException("El rol debe tener al menos un permiso asignado.");
        }

        this.id = id;
        this.nombre = nombre;
        this.permisos = Set.copyOf(permisos);
    }

    public static void validarReglasCreacion(String nombre, Set<Long> permisoIds) {
        if (nombre == null || nombre.isBlank()) {
            throw new RolInvalidoException("El nombre del rol no puede estar vacío.");
        }
        if (permisoIds == null || permisoIds.isEmpty()) {
            throw new RolInvalidoException("El rol debe tener al menos un permiso asignado.");
        }
    }
}