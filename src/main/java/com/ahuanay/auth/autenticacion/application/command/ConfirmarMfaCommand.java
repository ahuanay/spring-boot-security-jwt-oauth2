package com.ahuanay.auth.autenticacion.application.command;

public record ConfirmarMfaCommand(
    Long usuarioId,
    String codigo
) {
}
