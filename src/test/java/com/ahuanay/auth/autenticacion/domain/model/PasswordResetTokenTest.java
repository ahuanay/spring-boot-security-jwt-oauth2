package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetTokenTest {

    private static final String TOKEN_HASH = "hash-sha-256";
    private static final String EMAIL = "user@email.com";
    private static final LocalDateTime EXPIRACION = LocalDateTime.now().plusMinutes(30);

    @Test
    @DisplayName("Debería construir el token con sus datos y sin fecha de consumo cuando es nuevo")
    void deberiaConstruirToken_SinFechaDeConsumoCuandoEsNuevo() {
        // Arrange & Act
        PasswordResetToken token = new PasswordResetToken(1L, TOKEN_HASH, EMAIL, EXPIRACION, null);

        // Assert
        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(token.getEmail()).isEqualTo(EMAIL);
        assertThat(token.getExpiresAt()).isEqualTo(EXPIRACION);
        assertThat(token.getUsedAt()).isNull();
        assertThat(token.estaUsado()).isFalse();
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el hash está vacío")
    void deberiaLanzarExcepcion_CuandoElHashEstaVacio() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new PasswordResetToken(null, " ", EMAIL, EXPIRACION, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El hash del token de recuperación no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el email está vacío")
    void deberiaLanzarExcepcion_CuandoElEmailEstaVacio() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new PasswordResetToken(null, TOKEN_HASH, "", EXPIRACION, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El email asociado al token de recuperación no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando la expiración es nula")
    void deberiaLanzarExcepcion_CuandoLaExpiracionEsNula() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new PasswordResetToken(null, TOKEN_HASH, EMAIL, null, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("La expiración del token de recuperación no puede ser nula.");
    }

    @Test
    @DisplayName("Debería reconocer el token como expirado cuando la fecha de expiración pasó")
    void deberiaReconocerElTokenComoExpirado_CuandoLaExpiracionPaso() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(null, TOKEN_HASH, EMAIL, EXPIRACION, null);

        // Act & Assert
        assertThat(token.estaExpirado(EXPIRACION.plusNanos(1))).isTrue();
        assertThat(token.estaExpirado(EXPIRACION.minusNanos(1))).isFalse();
    }

    @Test
    @DisplayName("Debería marcar el token como usado solo una vez cuando se llama reiteradamente")
    void deberiaMarcarElTokenComoUsadoSoloUnaVezCuandoSeLlamaReiteradamente() {
        // Arrange
        LocalDateTime fechaConsumo = LocalDateTime.now();
        PasswordResetToken token = new PasswordResetToken(null, TOKEN_HASH, EMAIL, EXPIRACION, null);

        // Act
        token.marcarComoUsado(fechaConsumo);
        LocalDateTime otraFecha = fechaConsumo.plusHours(1);
        token.marcarComoUsado(otraFecha);

        // Assert
        assertThat(token.estaUsado()).isTrue();
        assertThat(token.getUsedAt()).isEqualTo(fechaConsumo);
    }
}
