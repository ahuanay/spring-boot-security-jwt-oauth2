package com.ahuanay.auth.autenticacion.application.response;

public record LoginResponse(
    Boolean mfaRequerido,
    AuthResponse credenciales,
    String tokenMfa,
    Long expiraEnSegundos
) {
    public static LoginResponse conCredenciales(AuthResponse credenciales) {
        return new LoginResponse(false, credenciales, null, null);
    }

    public static LoginResponse conOtpRequerido(String tokenMfa, Long expiraEnSegundos) {
        return new LoginResponse(true, null, tokenMfa, expiraEnSegundos);
    }
}
