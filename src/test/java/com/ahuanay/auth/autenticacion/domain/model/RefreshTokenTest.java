package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    private static final String TOKEN = "refresh-token-firmado";
    private static final String EMAIL = "user@email.com";
    private static final LocalDateTime EXPIRACION = LocalDateTime.now().plusDays(7);

    @Test
    @DisplayName("Debería construir el refresh token con todos sus datos")
    void deberiaConstruirRefreshToken_ConTodosSusDatos() {
        // Act
        RefreshToken token = new RefreshToken(1L, TOKEN, EMAIL, EXPIRACION);

        // Assert
        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getToken()).isEqualTo(TOKEN);
        assertThat(token.getEmail()).isEqualTo(EMAIL);
        assertThat(token.getExpiresAt()).isEqualTo(EXPIRACION);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token es nulo")
    void deberiaLanzarExcepcion_CuandoElTokenEsNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new RefreshToken(null, null, EMAIL, EXPIRACION))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token está en blanco")
    void deberiaLanzarExcepcion_CuandoElTokenEstaEnBlanco() {
        // Act & Assert
        assertThatThrownBy(() -> new RefreshToken(null, "   ", EMAIL, EXPIRACION))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el email asociado está vacío")
    void deberiaLanzarExcepcion_CuandoElEmailAsociadoEstaVacio() {
        // Act & Assert
        assertThatThrownBy(() -> new RefreshToken(null, TOKEN, "", EXPIRACION))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El email asociado al token no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando la expiración es nula")
    void deberiaLanzarExcepcion_CuandoLaExpiracionEsNula() {
        // Act & Assert
        assertThatThrownBy(() -> new RefreshToken(null, TOKEN, EMAIL, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("La expiración del token no puede ser nula.");
    }
}
