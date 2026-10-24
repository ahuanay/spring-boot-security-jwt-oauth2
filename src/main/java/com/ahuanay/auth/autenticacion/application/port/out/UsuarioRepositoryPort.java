package com.ahuanay.auth.autenticacion.application.port.out;

import com.ahuanay.auth.autenticacion.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(Long id);

    Usuario save(Usuario usuario);
}