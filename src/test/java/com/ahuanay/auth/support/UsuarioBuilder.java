package com.ahuanay.auth.support;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.rol.domain.model.Rol;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class UsuarioBuilder {

    private Long id = TestConstantes.ID_DEFECTO;
    private String email = TestConstantes.EMAIL;
    private String password = TestConstantes.HASH;
    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId = null;
    private Boolean active = true;
    private Boolean mfaEnabled = false;
    private final Set<Rol> roles = new LinkedHashSet<>(Set.of(TestRoles.paciente()));

    UsuarioBuilder() {
    }

    public UsuarioBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public UsuarioBuilder conEmail(String email) {
        this.email = email;
        return this;
    }

    public UsuarioBuilder conPassword(String password) {
        this.password = password;
        return this;
    }

    public UsuarioBuilder sinPassword() {
        this.password = null;
        return this;
    }

    public UsuarioBuilder conProvider(AuthProvider provider) {
        this.provider = provider;
        return this;
    }

    public UsuarioBuilder conProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public UsuarioBuilder activo() {
        this.active = true;
        return this;
    }

    public UsuarioBuilder inactivo() {
        this.active = false;
        return this;
    }

    public UsuarioBuilder conMfa() {
        this.mfaEnabled = true;
        return this;
    }

    public UsuarioBuilder sinMfa() {
        this.mfaEnabled = false;
        return this;
    }

    public UsuarioBuilder conRol(Rol rol) {
        this.roles.clear();
        this.roles.add(rol);
        return this;
    }

    public UsuarioBuilder conRoles(Rol... roles) {
        this.roles.addAll(Arrays.asList(roles));
        return this;
    }

    public UsuarioBuilder google() {
        this.provider = AuthProvider.GOOGLE;
        this.password = null;
        this.providerId = "google-id";
        return this;
    }

    public UsuarioBuilder github() {
        this.provider = AuthProvider.GITHUB;
        this.password = null;
        this.providerId = "github-id";
        return this;
    }

    public Usuario build() {
        return new Usuario(id, email, password, provider, providerId, active, mfaEnabled, roles);
    }
}
