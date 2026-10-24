package com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionDelegatorTest {

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private SecurityExceptionDelegator securityExceptionDelegator;

    @Test
    @DisplayName("Debería delegar la AuthenticationException al resolver cuando el endpoint requiere autenticación")
    void deberiaDelegarLaAuthenticationException_CuandoElEndpointRequiereAutenticacion() {
        // Arrange
        AuthenticationException excepcion = new BadCredentialsException("Credenciales inválidas.");

        // Act
        securityExceptionDelegator.commence(request, response, excepcion);

        // Assert
        verify(handlerExceptionResolver, times(1)).resolveException(request, response, null, excepcion);
    }

    @Test
    @DisplayName("Debería delegar la AccessDeniedException al resolver cuando el usuario carece de permisos")
    void deberiaDelegarLaAccessDeniedException_CuandoElUsuarioCareceDePermisos() {
        // Arrange
        AccessDeniedException excepcion = new AccessDeniedException("Acceso denegado.");

        // Act
        securityExceptionDelegator.handle(request, response, excepcion);

        // Assert
        verify(handlerExceptionResolver, times(1)).resolveException(request, response, null, excepcion);
    }
}