package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.PasswordResetTokenEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.PasswordResetTokenMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final SpringDataPasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenMapper passwordResetTokenMapper;

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return passwordResetTokenRepository.findByTokenHash(tokenHash).map(passwordResetTokenMapper::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = passwordResetTokenMapper.toEntity(token);
        PasswordResetTokenEntity saved = passwordResetTokenRepository.save(entity);
        return passwordResetTokenMapper.toDomain(saved);
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        passwordResetTokenRepository.deleteByTokenHash(tokenHash);
    }

    @Override
    public void deleteByEmail(String email) {
        passwordResetTokenRepository.deleteByEmail(email);
    }
}
