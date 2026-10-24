package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;

public interface OAuth2LoginUseCase {
    AuthResponse processOAuth2Login(String email, String providerId, AuthProvider provider);
}