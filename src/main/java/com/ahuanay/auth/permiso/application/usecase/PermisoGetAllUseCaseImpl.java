package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.permiso.application.port.in.PermisoGetAllUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoGetAllUseCaseImpl implements PermisoGetAllUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermisoResponse> getAll(PermisoQuery query) {
        Page<Permiso> permisoPage = permisoRepositoryPort.findAll(query);

        Page<PermisoResponse> responsePage = permisoPage.map(permiso ->
            new PermisoResponse(permiso.getId(), permiso.getNombre())
        );

        return new PageResponse<>(
            responsePage.getContent(),
            responsePage.getNumber(),
            responsePage.getSize(),
            responsePage.getTotalElements(),
            responsePage.getTotalPages(),
            responsePage.isLast()
        );
    }
}