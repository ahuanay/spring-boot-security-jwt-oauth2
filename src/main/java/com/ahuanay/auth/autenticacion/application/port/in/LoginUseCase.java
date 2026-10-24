package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.LoginCommand;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginCommand command);
}
