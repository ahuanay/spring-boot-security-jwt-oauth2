package com.ahuanay.auth.autenticacion.application.port.out;

import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;

public interface JwtServicePort {
    String generateAccessToken(Usuario usuario);

    RefreshTokenGenerado generateRefreshToken(Usuario usuario);

    String extractUsername(String token);

    boolean isTokenValid(String token, String username);
}