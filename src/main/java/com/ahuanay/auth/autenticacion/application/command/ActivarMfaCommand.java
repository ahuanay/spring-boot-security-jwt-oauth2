package com.ahuanay.auth.autenticacion.application.command;

public record ActivarMfaCommand(
    Long usuarioId,
    String password
) {
}
