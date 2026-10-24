package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.RefreshTokenEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.RefreshTokenMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final SpringDataRefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token).map(refreshTokenMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = refreshTokenMapper.toEntity(refreshToken);
        RefreshTokenEntity saved = refreshTokenRepository.save(entity);
        return refreshTokenMapper.toDomain(saved);
    }

    @Override
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Override
    public void deleteByEmail(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }
}