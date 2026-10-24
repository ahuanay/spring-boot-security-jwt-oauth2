package com.ahuanay.auth.autenticacion.application.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {
    public static final String TIPO_TOKEN = "Bearer";

    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, TIPO_TOKEN);
    }
}
