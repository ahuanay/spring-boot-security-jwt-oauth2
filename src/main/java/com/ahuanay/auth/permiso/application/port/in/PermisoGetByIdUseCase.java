package com.ahuanay.auth.permiso.application.port.in;

import com.ahuanay.auth.permiso.application.response.PermisoResponse;

public interface PermisoGetByIdUseCase {
    PermisoResponse getById(Long id);
}
