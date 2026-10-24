package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AutenticacionExceptionHandlerTest {

    private AutenticacionExceptionHandler autenticacionExceptionHandler;

    @BeforeEach
    void setUp() {
        autenticacionExceptionHandler = new AutenticacionExceptionHandler("America/Lima");
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized con la estructura de error estándar cuando las credenciales son inválidas")
    void deberiaRetornarUnauthorizedConEstructuraDeErrorEstandar_CuandoLasCredencialesSonInvalidas() {
        // Arrange
        CredencialInvalidoException excepcion = new CredencialInvalidoException("Credenciales inválidas.");

        // Act
        ResponseEntity<ErrorResponse> respuesta = autenticacionExceptionHandler.handleCredencialInvalido(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(401);
        assertThat(error.error()).isEqualTo("Unauthorized");
        assertThat(error.message()).isEqualTo("Credenciales inválidas.");
        assertThat(error.timestamp()).isNotNull();
        assertThat(error.errors()).isNull();
        assertThat(error.notFoundIds()).isNull();
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized preservando el mensaje cuando se rechaza el refresh token")
    void deberiaRetornarUnauthorizedPreservandoElMensaje_CuandoSeRechazaElRefreshToken() {
        // Arrange
        CredencialInvalidoException excepcion = new CredencialInvalidoException("Refresh token inválido o expirado.");

        // Act
        ResponseEntity<ErrorResponse> respuesta = autenticacionExceptionHandler.handleCredencialInvalido(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().message()).isEqualTo("Refresh token inválido o expirado.");
    }
}