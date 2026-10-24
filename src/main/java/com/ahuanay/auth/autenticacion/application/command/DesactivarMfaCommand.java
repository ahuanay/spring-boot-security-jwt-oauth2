package com.ahuanay.auth.autenticacion.application.command;

public record DesactivarMfaCommand(
    Long usuarioId,
    String password
) {
}
