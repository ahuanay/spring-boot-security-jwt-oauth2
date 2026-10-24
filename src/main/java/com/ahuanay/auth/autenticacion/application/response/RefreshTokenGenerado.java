package com.ahuanay.auth.autenticacion.application.response;

import java.time.LocalDateTime;

public record RefreshTokenGenerado(
    String token,
    LocalDateTime expiresAt
) {
}
