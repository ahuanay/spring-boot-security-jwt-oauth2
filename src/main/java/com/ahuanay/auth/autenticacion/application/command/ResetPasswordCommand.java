package com.ahuanay.auth.autenticacion.application.command;

public record ResetPasswordCommand(
    String token,
    String newPassword
) {
}
