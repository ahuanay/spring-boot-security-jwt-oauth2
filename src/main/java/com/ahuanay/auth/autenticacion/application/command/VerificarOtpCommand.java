package com.ahuanay.auth.autenticacion.application.command;

public record VerificarOtpCommand(
    String tokenMfa,
    String codigo
) {
}
