package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.autenticacion.application.port.in.OAuth2LoginUseCase;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy.OAuth2StrategyRegistry;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy.OAuth2UserInfoStrategy;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final OAuth2LoginUseCase oAuth2LoginUseCase;
    private final ObjectMapper objectMapper;
    private final OAuth2StrategyRegistry strategyRegistry;

    @Value("${app.timezone:America/Lima}")
    private String timezone;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            AuthProvider provider = mapToAuthProvider(registrationId);

            OAuth2UserInfoStrategy strategy = strategyRegistry.getStrategy(provider);

            OAuth2User oauthUser = oauthToken.getPrincipal();

            String email = strategy.extractEmail(oauthUser);
            String providerId = strategy.extractProviderId(oauthUser);

            AuthResponse authResponse = oAuth2LoginUseCase.processOAuth2Login(email, providerId, provider);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), authResponse);
        } catch (UsuarioInvalidoException ex) {
            LOGGER.warn("OAuth2 login denied: {}", ex.getMessage());
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during OAuth2 login: {}", ex.getMessage(), ex);
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error durante el inicio de sesión con OAuth2.");
        }
    }

    private AuthProvider mapToAuthProvider(String registrationId) {
        if ("github".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GITHUB;
        } else if ("google".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GOOGLE;
        }
        throw new UsuarioInvalidoException("Proveedor OAuth2 no soportado: " + registrationId);
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(ZoneId.of(timezone)),
            status.value(),
            status.getReasonPhrase(),
            message,
            null,
            null
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
