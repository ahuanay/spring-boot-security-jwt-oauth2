package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2FailureHandlerTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private OAuth2FailureHandler oAuth2FailureHandler;

    private ObjectMapper objectMapper;
    private MockHttpServletResponse httpServletResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        oAuth2FailureHandler = new OAuth2FailureHandler(objectMapper);
        ReflectionTestUtils.setField(oAuth2FailureHandler, "timezone", "America/Lima");
        httpServletResponse = new MockHttpServletResponse();
    }

    private JsonNode leerCuerpo() throws Exception {
        return objectMapper.readTree(httpServletResponse.getContentAsString());
    }

    @Test
    @DisplayName("Debería responder 401 con la descripción del error cuando el proveedor OAuth2 envía un motivo")
    void deberiaResponder401ConLaDescripcionDelError_CuandoElProveedorEnviaUnMotivo() throws Exception {
        // Arrange
        OAuth2Error oAuth2Error = new OAuth2Error(
            "access_denied",
            "El usuario canceló el inicio de sesión en el proveedor.",
            null);
        AuthenticationException excepcion = new OAuth2AuthenticationException(oAuth2Error);

        // Act
        oAuth2FailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, excepcion);

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(401);
        assertThat(httpServletResponse.getContentType()).contains("application/json");

        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("status").asInt()).isEqualTo(401);
        assertThat(cuerpo.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(cuerpo.get("message").asText())
            .isEqualTo("El usuario canceló el inicio de sesión en el proveedor.");
    }

    @Test
    @DisplayName("Debería responder 401 con mensaje genérico cuando el error OAuth2 no trae descripción")
    void deberiaResponder401ConMensajeGenerico_CuandoElErrorOAuth2NoTraeDescripcion() throws Exception {
        // Arrange
        AuthenticationException excepcion = new OAuth2AuthenticationException(new OAuth2Error("server_error"));

        // Act
        oAuth2FailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, excepcion);

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(401);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("message").asText()).isEqualTo("No se pudo completar el inicio de sesión con OAuth2.");
    }

    @Test
    @DisplayName("Debería responder 401 con mensaje genérico cuando la excepción no es de OAuth2")
    void deberiaResponder401ConMensajeGenerico_CuandoLaExcepcionNoEsDeOAuth2() throws Exception {
        // Arrange
        AuthenticationException excepcion = new BadCredentialsException("Credenciales inválidas.");

        // Act
        oAuth2FailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, excepcion);

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(401);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("message").asText()).isEqualTo("No se pudo completar el inicio de sesión con OAuth2.");
    }

    @Test
    @DisplayName("Debería no escribir la respuesta cuando la respuesta ya fue confirmada")
    void deberiaNoEscribirError_CuandoLaRespuestaYaFueConfirmada() throws Exception {
        // Arrange
        HttpServletResponse respuestaConfirmada = mock(HttpServletResponse.class);
        when(respuestaConfirmada.isCommitted()).thenReturn(true);
        AuthenticationException excepcion = new OAuth2AuthenticationException(
            new OAuth2Error("access_denied", "El usuario canceló el inicio de sesión en el proveedor.", null));

        // Act
        oAuth2FailureHandler.onAuthenticationFailure(httpServletRequest, respuestaConfirmada, excepcion);

        // Assert
        verify(respuestaConfirmada, never()).setStatus(anyInt());
        verify(respuestaConfirmada, never()).getWriter();
    }
}