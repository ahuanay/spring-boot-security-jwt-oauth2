package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.autenticacion.application.port.in.OAuth2LoginUseCase;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy.OAuth2StrategyRegistry;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy.OAuth2UserInfoStrategy;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    private static final String EMAIL = "useroauth2@gmail.com";
    private static final String PROVIDER_ID = "google-123";
    private static final String ACCESS_TOKEN = "access-token-123";
    private static final String REFRESH_TOKEN = "refresh-token-456";

    @Mock
    private OAuth2LoginUseCase oAuth2LoginUseCase;

    @Mock
    private OAuth2StrategyRegistry oAuth2StrategyRegistry;

    @Mock
    private OAuth2UserInfoStrategy oAuth2UserInfoStrategy;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private ObjectMapper objectMapper;
    private MockHttpServletResponse httpServletResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        oAuth2SuccessHandler = new OAuth2SuccessHandler(oAuth2LoginUseCase, objectMapper, oAuth2StrategyRegistry);
        ReflectionTestUtils.setField(oAuth2SuccessHandler, "timezone", "America/Lima");
        httpServletResponse = new MockHttpServletResponse();
        authResponse = new AuthResponse(ACCESS_TOKEN, REFRESH_TOKEN);
    }

    private OAuth2AuthenticationToken crearTokenOAuth2(OAuth2User principal, String registrationId) {
        return new OAuth2AuthenticationToken(
            principal,
            Set.of(new SimpleGrantedAuthority("ROLE_USER")),
            registrationId
        );
    }

    private OAuth2User crearUsuarioGoogle() {
        return mock(OAuth2User.class);
    }

    private JsonNode leerCuerpo() throws Exception {
        return objectMapper.readTree(httpServletResponse.getContentAsString());
    }

    @Test
    @DisplayName("Debería responder los tokens cuando el login con Google es exitoso")
    void deberiaResponderTokens_CuandoElLoginConGoogleEsExitoso() throws Exception {
        // Arrange
        when(oAuth2StrategyRegistry.getStrategy(AuthProvider.GOOGLE)).thenReturn(oAuth2UserInfoStrategy);
        when(oAuth2UserInfoStrategy.extractEmail(any(OAuth2User.class))).thenReturn(EMAIL);
        when(oAuth2UserInfoStrategy.extractProviderId(any(OAuth2User.class))).thenReturn(PROVIDER_ID);
        when(oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE)).thenReturn(authResponse);

        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, httpServletResponse, crearTokenOAuth2(crearUsuarioGoogle(), "google"));

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(200);
        assertThat(httpServletResponse.getContentType()).contains("application/json");

        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("accessToken").asText()).isEqualTo(ACCESS_TOKEN);
        assertThat(cuerpo.get("refreshToken").asText()).isEqualTo(REFRESH_TOKEN);
        assertThat(cuerpo.get("tokenType").asText()).isEqualTo("Bearer");

        verify(oAuth2LoginUseCase, times(1)).processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Debería responder los tokens cuando el login con GitHub es exitoso")
    void deberiaResponderTokens_CuandoElLoginConGithubEsExitoso() throws Exception {
        // Arrange
        OAuth2User githubUser = mock(OAuth2User.class);
        when(oAuth2StrategyRegistry.getStrategy(AuthProvider.GITHUB)).thenReturn(oAuth2UserInfoStrategy);
        when(oAuth2UserInfoStrategy.extractEmail(githubUser)).thenReturn(EMAIL);
        when(oAuth2UserInfoStrategy.extractProviderId(githubUser)).thenReturn(PROVIDER_ID);
        when(oAuth2LoginUseCase.processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GITHUB)).thenReturn(authResponse);

        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, httpServletResponse, crearTokenOAuth2(githubUser, "github"));

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(200);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("accessToken").asText()).isEqualTo(ACCESS_TOKEN);
        assertThat(cuerpo.get("tokenType").asText()).isEqualTo("Bearer");

        verify(oAuth2LoginUseCase, times(1)).processOAuth2Login(EMAIL, PROVIDER_ID, AuthProvider.GITHUB);
    }

    @Test
    @DisplayName("Debería responder 401 con el mensaje cuando el login es rechazado por UsuarioInvalidoException")
    void deberiaResponder401ConMensaje_CuandoElLoginEsRechazadoPorUsuarioInvalidoException() throws Exception {
        // Arrange
        when(oAuth2StrategyRegistry.getStrategy(AuthProvider.GOOGLE)).thenReturn(oAuth2UserInfoStrategy);
        when(oAuth2UserInfoStrategy.extractEmail(any(OAuth2User.class))).thenReturn(EMAIL);
        when(oAuth2LoginUseCase.processOAuth2Login(any(), any(), any()))
            .thenThrow(new UsuarioInvalidoException("El usuario se encuentra inactivo."));

        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, httpServletResponse, crearTokenOAuth2(crearUsuarioGoogle(), "google"));

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(401);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("status").asInt()).isEqualTo(401);
        assertThat(cuerpo.get("message").asText()).isEqualTo("El usuario se encuentra inactivo.");
        assertThat(cuerpo.has("accessToken")).isFalse();
    }

    @Test
    @DisplayName("Debería responder 401 sin consultar el registry cuando el proveedor no está soportado")
    void deberiaResponder401SinConsultarElRegistry_CuandoElProveedorNoEstaSoportado() throws Exception {
        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, httpServletResponse, crearTokenOAuth2(mock(OAuth2User.class), "facebook"));

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(401);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("status").asInt()).isEqualTo(401);
        assertThat(cuerpo.get("message").asText()).isEqualTo("Proveedor OAuth2 no soportado: facebook");

        verifyNoInteractions(oAuth2StrategyRegistry, oAuth2LoginUseCase);
    }

    @Test
    @DisplayName("Debería responder 500 genérico cuando ocurre un error inesperado durante el login")
    void deberiaResponder500Generico_CuandoOcurreUnErrorInesperadoDuranteElLogin() throws Exception {
        // Arrange
        when(oAuth2StrategyRegistry.getStrategy(AuthProvider.GOOGLE)).thenReturn(oAuth2UserInfoStrategy);
        when(oAuth2UserInfoStrategy.extractEmail(any(OAuth2User.class))).thenReturn(EMAIL);
        when(oAuth2LoginUseCase.processOAuth2Login(any(), any(), any()))
            .thenThrow(new RuntimeException("Error de base de datos simulado"));

        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, httpServletResponse, crearTokenOAuth2(crearUsuarioGoogle(), "google"));

        // Assert
        assertThat(httpServletResponse.getStatus()).isEqualTo(500);
        JsonNode cuerpo = leerCuerpo();
        assertThat(cuerpo.get("status").asInt()).isEqualTo(500);
        assertThat(cuerpo.get("message").asText()).isEqualTo("Ocurrió un error durante el inicio de sesión con OAuth2.");
        assertThat(cuerpo.has("accessToken")).isFalse();
    }

    @Test
    @DisplayName("Debería no escribir la respuesta de error cuando la respuesta ya fue confirmada")
    void deberiaNoEscribirError_CuandoLaRespuestaYaFueConfirmada() throws Exception {
        // Arrange
        HttpServletResponse respuestaConfirmada = mock(HttpServletResponse.class);
        when(respuestaConfirmada.isCommitted()).thenReturn(true);
        when(oAuth2StrategyRegistry.getStrategy(AuthProvider.GOOGLE)).thenReturn(oAuth2UserInfoStrategy);
        when(oAuth2UserInfoStrategy.extractEmail(any(OAuth2User.class))).thenReturn(EMAIL);
        when(oAuth2LoginUseCase.processOAuth2Login(any(), any(), any()))
            .thenThrow(new UsuarioInvalidoException("El usuario se encuentra inactivo."));

        // Act
        oAuth2SuccessHandler.onAuthenticationSuccess(
            httpServletRequest, respuestaConfirmada, crearTokenOAuth2(crearUsuarioGoogle(), "google"));

        // Assert
        verify(respuestaConfirmada, never()).setStatus(anyInt());
        verify(respuestaConfirmada, never()).getWriter();
    }
}