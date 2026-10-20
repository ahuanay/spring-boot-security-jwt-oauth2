package com.ahuanay.auth.rol.application.port.in;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.application.response.RolResponse;

public interface RolGetAllUseCase {
    PageResponse<RolResponse> getAll(RolQuery query);
}