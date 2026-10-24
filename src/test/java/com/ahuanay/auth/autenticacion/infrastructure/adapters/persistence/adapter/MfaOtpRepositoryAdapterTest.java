package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.MfaOtpEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.MfaOtpMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataMfaOtpRepository;
import com.ahuanay.auth.support.TestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaOtpRepositoryAdapterTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_HASH = "hash-token-mfa";

    @Mock
    private SpringDataMfaOtpRepository mfaOtpRepository;

    @Mock
    private MfaOtpMapper mfaOtpMapper;

    @InjectMocks
    private MfaOtpRepositoryAdapter mfaOtpRepositoryAdapter;

    private MfaOtp mfaOtpDomain;
    private MfaOtpEntity mfaOtpEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(5);
        mfaOtpDomain = TestTokens.otp(TOKEN_HASH, EMAIL, "hash-codigo", PropositoOtp.LOGIN, expiracion);
        mfaOtpEntity = new MfaOtpEntity(
            1L, TOKEN_HASH, EMAIL, "hash-codigo", PropositoOtp.LOGIN, expiracion, 0, null);
    }

    @Test
    @DisplayName("Debería retornar el OTP cuando se busca por token hash existente")
    void deberiaRetornarOtp_CuandoSeBuscaPorTokenHashExistente() {
        // Arrange
        when(mfaOtpRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(mfaOtpEntity));
        when(mfaOtpMapper.toDomain(mfaOtpEntity)).thenReturn(mfaOtpDomain);

        // Act
        Optional<MfaOtp> resultado = mfaOtpRepositoryAdapter.findByTokenHash(TOKEN_HASH);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
        verify(mfaOtpRepository, times(1)).findByTokenHash(TOKEN_HASH);
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el token hash no existe")
    void deberiaRetornarVacio_CuandoElTokenHashNoExiste() {
        // Arrange
        when(mfaOtpRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act
        Optional<MfaOtp> resultado = mfaOtpRepositoryAdapter.findByTokenHash(TOKEN_HASH);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debería retornar el OTP cuando se busca por email y propósito")
    void deberiaRetornarOtp_CuandoSeBuscaPorEmailYProposito() {
        // Arrange
        when(mfaOtpRepository.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION))
            .thenReturn(Optional.of(mfaOtpEntity));
        when(mfaOtpMapper.toDomain(mfaOtpEntity)).thenReturn(mfaOtpDomain);

        // Act
        Optional<MfaOtp> resultado = mfaOtpRepositoryAdapter.findByEmailAndProposito(EMAIL, PropositoOtp.ACTIVACION);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("Debería persistir el OTP cuando se invoca el método save delegando en el mapper y el repositorio")
    void deberiaPersistirOtp_CuandoSeInvocaSaveDelegandoEnMapperYRepositorio() {
        // Arrange
        when(mfaOtpMapper.toEntity(mfaOtpDomain)).thenReturn(mfaOtpEntity);
        when(mfaOtpRepository.save(mfaOtpEntity)).thenReturn(mfaOtpEntity);
        when(mfaOtpMapper.toDomain(mfaOtpEntity)).thenReturn(mfaOtpDomain);

        // Act
        MfaOtp resultado = mfaOtpRepositoryAdapter.save(mfaOtpDomain);

        // Assert
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(mfaOtpMapper, times(1)).toEntity(mfaOtpDomain);
        verify(mfaOtpRepository, times(1)).save(mfaOtpEntity);
    }

    @Test
    @DisplayName("Debería eliminar los registros cuando se invoca la eliminación por token hash, por email y por email con propósito")
    void deberiaEliminarRegistros_CuandoSeInvocaEliminacionPorTokenHashEmailYProposito() {
        // Act
        mfaOtpRepositoryAdapter.deleteByTokenHash(TOKEN_HASH);
        mfaOtpRepositoryAdapter.deleteByEmail(EMAIL);
        mfaOtpRepositoryAdapter.deleteByEmailAndProposito(EMAIL, PropositoOtp.LOGIN);

        // Assert
        verify(mfaOtpRepository, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(mfaOtpRepository, times(1)).deleteByEmail(EMAIL);
        verify(mfaOtpRepository, times(1)).deleteByEmailAndProposito(EMAIL, PropositoOtp.LOGIN);
    }
}