package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2FailureHandler.class);

    private final ObjectMapper objectMapper;

    @Value("${app.timezone:America/Lima}")
    private String timezone;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        LOGGER.warn("OAuth2 login failed: {}", exception.getMessage());

        String message = resolverMensaje(exception);
        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
    }

    private String resolverMensaje(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String descripcion = oauth2Exception.getError().getDescription();
            if (descripcion != null && !descripcion.isBlank()) {
                return descripcion;
            }
        }
        return "No se pudo completar el inicio de sesión con OAuth2.";
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
