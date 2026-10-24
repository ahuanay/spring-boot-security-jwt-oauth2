package com.ahuanay.auth.autenticacion.application.command;

import java.util.Set;

public record RegisterCommand(
    String email,
    String password,
    Set<Long> roleIds
) {
}