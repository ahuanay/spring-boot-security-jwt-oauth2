package com.ahuanay.auth.autenticacion.application.command;

public record LoginCommand(
    String email,
    String password
) {
}