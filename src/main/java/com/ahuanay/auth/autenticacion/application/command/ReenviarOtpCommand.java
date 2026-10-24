package com.ahuanay.auth.autenticacion.application.command;

public record ReenviarOtpCommand(
    String tokenMfa
) {
}
