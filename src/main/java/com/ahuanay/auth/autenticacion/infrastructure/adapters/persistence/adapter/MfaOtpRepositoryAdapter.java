package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.MfaOtpEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.MfaOtpMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataMfaOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MfaOtpRepositoryAdapter implements MfaOtpRepositoryPort {

    private final SpringDataMfaOtpRepository mfaOtpRepository;
    private final MfaOtpMapper mfaOtpMapper;

    @Override
    public Optional<MfaOtp> findByTokenHash(String tokenHash) {
        return mfaOtpRepository.findByTokenHash(tokenHash).map(mfaOtpMapper::toDomain);
    }

    @Override
    public Optional<MfaOtp> findByEmailAndProposito(String email, PropositoOtp proposito) {
        return mfaOtpRepository.findByEmailAndProposito(email, proposito).map(mfaOtpMapper::toDomain);
    }

    @Override
    public MfaOtp save(MfaOtp mfaOtp) {
        MfaOtpEntity entity = mfaOtpMapper.toEntity(mfaOtp);
        MfaOtpEntity saved = mfaOtpRepository.save(entity);
        return mfaOtpMapper.toDomain(saved);
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        mfaOtpRepository.deleteByTokenHash(tokenHash);
    }

    @Override
    public void deleteByEmail(String email) {
        mfaOtpRepository.deleteByEmail(email);
    }

    @Override
    public void deleteByEmailAndProposito(String email, PropositoOtp proposito) {
        mfaOtpRepository.deleteByEmailAndProposito(email, proposito);
    }
}
