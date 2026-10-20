package com.ahuanay.auth.rol.application.port.in;

import com.ahuanay.auth.rol.application.response.RolResponse;

public interface RolGetByIdUseCase {
    RolResponse getById(Long id);
}