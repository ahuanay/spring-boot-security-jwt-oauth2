package com.ahuanay.auth.rol.application.port.in;

import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.response.RolResponse;

public interface RolCreateUseCase {
    RolResponse create(RolCommand command);
}