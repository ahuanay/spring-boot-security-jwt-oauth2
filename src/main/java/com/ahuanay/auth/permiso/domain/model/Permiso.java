package com.ahuanay.auth.permiso.domain.model;

import com.ahuanay.auth.permiso.domain.exception.PermisoInvalidoException;
import lombok.Getter;

@Getter
public class Permiso {

    private final Long id;

    private final String nombre;

    public Permiso(Long id, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new PermisoInvalidoException("El nombre del permiso no puede estar vacío.");
        }

        this.id = id;
        this.nombre = nombre;
    }
}
