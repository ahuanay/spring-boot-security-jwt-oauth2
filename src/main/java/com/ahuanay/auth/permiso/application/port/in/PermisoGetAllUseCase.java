package com.ahuanay.auth.permiso.application.port.in;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;

public interface PermisoGetAllUseCase {
    PageResponse<PermisoResponse> getAll(PermisoQuery query);
}
