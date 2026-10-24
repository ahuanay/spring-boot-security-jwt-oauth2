package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.PasswordResetTokenEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.PasswordResetTokenMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataPasswordResetTokenRepository;
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
class PasswordResetTokenRepositoryAdapterTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_HASH = "hash-sha-256";

    @Mock
    private SpringDataPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetTokenMapper passwordResetTokenMapper;

    @InjectMocks
    private PasswordResetTokenRepositoryAdapter passwordResetTokenRepositoryAdapter;

    private PasswordResetToken passwordResetTokenDomain;
    private PasswordResetTokenEntity passwordResetTokenEntity;

    @BeforeEach
    void setUp() {
        passwordResetTokenDomain = TestTokens.resetToken(TOKEN_HASH, EMAIL, LocalDateTime.now().plusMinutes(30));
        passwordResetTokenEntity = new PasswordResetTokenEntity(1L, TOKEN_HASH, EMAIL, LocalDateTime.now().plusMinutes(30), null);
    }

    @Test
    @DisplayName("Debería retornar el token de recuperación cuando se busca por un hash existente")
    void deberiaRetornarPasswordResetToken_CuandoSeBuscaPorHashExistente() {
        // Arrange
        when(passwordResetTokenRepository.findByTokenHash(TOKEN_HASH))
            .thenReturn(Optional.of(passwordResetTokenEntity));
        when(passwordResetTokenMapper.toDomain(passwordResetTokenEntity)).thenReturn(passwordResetTokenDomain);

        // Act
        Optional<PasswordResetToken> resultado = passwordResetTokenRepositoryAdapter.findByTokenHash(TOKEN_HASH);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        assertThat(resultado.get().getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.get().estaUsado()).isFalse();

        verify(passwordResetTokenRepository, times(1)).findByTokenHash(TOKEN_HASH);
        verify(passwordResetTokenMapper, times(1)).toDomain(passwordResetTokenEntity);
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el hash del token de recuperación no existe")
    void deberiaRetornarVacio_CuandoElHashNoExiste() {
        // Arrange
        when(passwordResetTokenRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act
        Optional<PasswordResetToken> resultado = passwordResetTokenRepositoryAdapter.findByTokenHash(TOKEN_HASH);

        // Assert
        assertThat(resultado).isEmpty();

        verify(passwordResetTokenRepository, times(1)).findByTokenHash(TOKEN_HASH);
        verifyNoInteractions(passwordResetTokenMapper);
    }

    @Test
    @DisplayName("Debería retornar el token de recuperación guardado cuando se invoca save con fecha de consumo actualizada")
    void deberiaRetornarPasswordResetTokenGuardado_CuandoSeGuardaConFechaDeConsumoActualizada() {
        // Arrange
        LocalDateTime fechaConsumo = LocalDateTime.now();
        PasswordResetToken tokenUsado = TestTokens.resetTokenDetalle(1L, TOKEN_HASH, EMAIL,
            passwordResetTokenDomain.getExpiresAt(), fechaConsumo);
        PasswordResetTokenEntity entityUsado = new PasswordResetTokenEntity(1L, TOKEN_HASH, EMAIL,
            passwordResetTokenDomain.getExpiresAt(), fechaConsumo);
        when(passwordResetTokenMapper.toEntity(tokenUsado)).thenReturn(entityUsado);
        when(passwordResetTokenRepository.save(entityUsado)).thenReturn(entityUsado);
        when(passwordResetTokenMapper.toDomain(entityUsado)).thenReturn(tokenUsado);

        // Act
        PasswordResetToken resultado = passwordResetTokenRepositoryAdapter.save(tokenUsado);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.estaUsado()).isTrue();

        verify(passwordResetTokenMapper, times(1)).toEntity(tokenUsado);
        verify(passwordResetTokenRepository, times(1)).save(entityUsado);
        verify(passwordResetTokenMapper, times(1)).toDomain(entityUsado);
    }

    @Test
    @DisplayName("Debería eliminar el token de recuperación cuando se revoca por su hash")
    void deberiaEliminarPasswordResetToken_CuandoSeRevocaPorSuHash() {
        // Act
        passwordResetTokenRepositoryAdapter.deleteByTokenHash(TOKEN_HASH);

        // Assert
        verify(passwordResetTokenRepository, times(1)).deleteByTokenHash(TOKEN_HASH);
        verifyNoInteractions(passwordResetTokenMapper);
    }

    @Test
    @DisplayName("Debería eliminar todos los tokens de recuperación del email cuando se solicita un nuevo envío")
    void deberiaEliminarTokensPorEmail_CuandoSeSolicitaNuevoEnvio() {
        // Act
        passwordResetTokenRepositoryAdapter.deleteByEmail(EMAIL);

        // Assert
        verify(passwordResetTokenRepository, times(1)).deleteByEmail(EMAIL);
        verifyNoInteractions(passwordResetTokenMapper);
    }
}