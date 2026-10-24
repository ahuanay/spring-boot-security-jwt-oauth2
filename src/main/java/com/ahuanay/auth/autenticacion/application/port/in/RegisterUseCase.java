package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.RegisterCommand;
import com.ahuanay.auth.autenticacion.application.response.UsuarioResponse;

public interface RegisterUseCase {
    UsuarioResponse register(RegisterCommand command);
}