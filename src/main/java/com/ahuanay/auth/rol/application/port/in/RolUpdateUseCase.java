package com.ahuanay.auth.rol.application.port.in;

import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.response.RolResponse;

public interface RolUpdateUseCase {
    RolResponse update(Long id, RolCommand command);
}