package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.Getter;

import java.util.Set;

@Getter
public class Usuario {

    private final Long id;
    private final String email;
    private final String password;
    private final AuthProvider provider;
    private final String providerId;
    private final Boolean active;
    private final Boolean mfaEnabled;
    private final Set<Rol> roles;

    public Usuario(
        Long id,
        String email,
        String password,
        AuthProvider provider,
        String providerId,
        Boolean active,
        Boolean mfaEnabled,
        Set<Rol> roles
    ) {
        if (email == null || email.isBlank()) {
            throw new UsuarioInvalidoException("El email del usuario no puede estar vacío.");
        }

        AuthProvider resolvedProvider = (provider == null) ? AuthProvider.LOCAL : provider;

        if (resolvedProvider == AuthProvider.LOCAL && (password == null || password.isBlank())) {
            throw new UsuarioInvalidoException("La contraseña del usuario no puede ser nula o vacía.");
        }

        if (roles == null || roles.isEmpty()) {
            throw new UsuarioInvalidoException("El usuario debe tener al menos un rol asignado.");
        }

        this.id = id;
        this.email = email;
        this.password = password;
        this.provider = resolvedProvider;
        this.providerId = providerId;
        this.active = active == null || active;
        this.mfaEnabled = Boolean.TRUE.equals(mfaEnabled);
        this.roles = Set.copyOf(roles);
    }

    public static void validarReglasCreacion(String email, String password, AuthProvider provider, Set<Long> rolIds) {
        if (email == null || email.isBlank()) {
            throw new UsuarioInvalidoException("El email del usuario no puede estar vacío.");
        }

        AuthProvider resolvedProvider = (provider == null) ? AuthProvider.LOCAL : provider;

        if (resolvedProvider == AuthProvider.LOCAL && (password == null || password.isBlank())) {
            throw new UsuarioInvalidoException("La contraseña del usuario no puede ser nula o vacía.");
        }

        if (resolvedProvider == AuthProvider.LOCAL && !ReglaContrasena.esSegura(password)) {
            throw new UsuarioInvalidoException(ReglaContrasena.MENSAJE);
        }

        if (rolIds == null || rolIds.isEmpty()) {
            throw new UsuarioInvalidoException("El usuario debe tener al menos un rol asignado.");
        }
    }
}