package com.ahuanay.auth.permiso.application.port.in;

import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;

public interface PermisoUpdateUseCase {
    PermisoResponse update(Long id, PermisoCommand command);
}
