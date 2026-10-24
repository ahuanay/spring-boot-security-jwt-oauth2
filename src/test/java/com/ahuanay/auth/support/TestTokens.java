package com.ahuanay.auth.support;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;

import java.time.LocalDateTime;

public final class TestTokens {

    public static LocalDateTime expiracionFutura() {
        return LocalDateTime.now().plusDays(7);
    }

    public static LocalDateTime expiracionPasada() {
        return LocalDateTime.now().minusMinutes(1);
    }

    public static RefreshToken refreshToken(String token, String email, LocalDateTime expiresAt) {
        return new RefreshToken(1L, token, email, expiresAt);
    }

    public static RefreshToken refreshTokenValido(String token, String email) {
        return refreshToken(token, email, expiracionFutura());
    }

    public static RefreshToken refreshTokenExpirado(String token, String email) {
        return refreshToken(token, email, expiracionPasada());
    }

    public static PasswordResetToken resetToken(String tokenHash, String email, LocalDateTime expiresAt) {
        return resetTokenDetalle(1L, tokenHash, email, expiresAt, null);
    }

    public static PasswordResetToken resetTokenUsado(String tokenHash, String email, LocalDateTime expiresAt) {
        return resetTokenDetalle(1L, tokenHash, email, expiresAt, LocalDateTime.now());
    }

    public static PasswordResetToken resetTokenDetalle(Long id, String tokenHash, String email,
                                                       LocalDateTime expiresAt, LocalDateTime usedAt) {
        return new PasswordResetToken(id, tokenHash, email, expiresAt, usedAt);
    }

    public static MfaOtp otp(String tokenHash, String email, String codigoHash, PropositoOtp proposito,
                             LocalDateTime expiresAt) {
        return otpCompleto(tokenHash, email, codigoHash, proposito, expiresAt, 0, null);
    }
    
    public static MfaOtp otpCompleto(String tokenHash, String email, String codigoHash, PropositoOtp proposito,
                                     LocalDateTime expiresAt, int intentos, LocalDateTime usedAt) {
        return new MfaOtp(1L, tokenHash, email, codigoHash, proposito, expiresAt, intentos, usedAt);
    }

    private TestTokens() {
    }
}
