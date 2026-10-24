package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.UsuarioEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.UsuarioMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = usuarioMapper.toEntity(usuario);
        UsuarioEntity saved = usuarioRepository.save(entity);
        return usuarioMapper.toDomain(saved);
    }
}