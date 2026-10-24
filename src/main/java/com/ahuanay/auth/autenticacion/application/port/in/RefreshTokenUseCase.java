package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.RefreshTokenCommand;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;

public interface RefreshTokenUseCase {
    AuthResponse refreshToken(RefreshTokenCommand command);
}