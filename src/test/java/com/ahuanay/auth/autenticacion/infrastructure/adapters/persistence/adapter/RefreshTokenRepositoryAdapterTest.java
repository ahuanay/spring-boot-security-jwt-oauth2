package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.RefreshTokenEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.RefreshTokenMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataRefreshTokenRepository;
import com.ahuanay.auth.support.TestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN = "refresh-token-123";

    @Mock
    private SpringDataRefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @InjectMocks
    private RefreshTokenRepositoryAdapter refreshTokenRepositoryAdapter;

    private RefreshToken refreshTokenDomain;
    private RefreshTokenEntity refreshTokenEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime expiracionFutura = LocalDateTime.now().plusDays(7);
        refreshTokenDomain = TestTokens.refreshToken(TOKEN, EMAIL, expiracionFutura);
        refreshTokenEntity = new RefreshTokenEntity(1L, TOKEN, EMAIL, expiracionFutura);
    }

    @Test
    @DisplayName("Debería retornar el token de refresh cuando se busca por un token existente")
    void deberiaRetornarRefreshToken_CuandoSeBuscaPorTokenExistente() {
        // Arrange
        when(refreshTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(refreshTokenEntity));
        when(refreshTokenMapper.toDomain(refreshTokenEntity)).thenReturn(refreshTokenDomain);

        // Act
        Optional<RefreshToken> resultado = refreshTokenRepositoryAdapter.findByToken(TOKEN);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        assertThat(resultado.get().getToken()).isEqualTo(TOKEN);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);

        verify(refreshTokenRepository, times(1)).findByToken(TOKEN);
        verify(refreshTokenMapper, times(1)).toDomain(refreshTokenEntity);
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el token de refresh no existe")
    void deberiaRetornarVacio_CuandoElRefreshTokenNoExiste() {
        // Arrange
        when(refreshTokenRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> resultado = refreshTokenRepositoryAdapter.findByToken(TOKEN);

        // Assert
        assertThat(resultado).isEmpty();

        verify(refreshTokenRepository, times(1)).findByToken(TOKEN);
        verifyNoInteractions(refreshTokenMapper);
    }

    @Test
    @DisplayName("Debería retornar el token de refresh guardado cuando los datos son válidos")
    void deberiaRetornarRefreshTokenGuardado_CuandoLosDatosSonValidos() {
        // Arrange
        when(refreshTokenMapper.toEntity(refreshTokenDomain)).thenReturn(refreshTokenEntity);
        when(refreshTokenRepository.save(refreshTokenEntity)).thenReturn(refreshTokenEntity);
        when(refreshTokenMapper.toDomain(refreshTokenEntity)).thenReturn(refreshTokenDomain);

        // Act
        RefreshToken resultado = refreshTokenRepositoryAdapter.save(refreshTokenDomain);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getToken()).isEqualTo(TOKEN);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);

        ArgumentCaptor<RefreshTokenEntity> entityCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenMapper, times(1)).toEntity(refreshTokenDomain);
        verify(refreshTokenRepository, times(1)).save(entityCaptor.capture());
        verify(refreshTokenMapper, times(1)).toDomain(refreshTokenEntity);
        assertThat(entityCaptor.getValue().getToken()).isEqualTo(TOKEN);
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("Debería eliminar el token de refresh cuando se proporciona un token válido")
    void deberiaEliminarRefreshToken_CuandoSeProporcionaTokenValido() {
        // Act
        refreshTokenRepositoryAdapter.deleteByToken(TOKEN);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteByToken(TOKEN);
        verifyNoInteractions(refreshTokenMapper);
    }

    @Test
    @DisplayName("Debería eliminar todos los tokens de refresh de un email cuando se revocan sus sesiones")
    void deberiaEliminarTokensPorEmail_CuandoSeRevocanSusSesiones() {
        // Act
        refreshTokenRepositoryAdapter.deleteByEmail(EMAIL);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteByEmail(EMAIL);
        verifyNoInteractions(refreshTokenMapper);
    }
}