package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.rol.application.port.in.RolGetAllUseCase;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolGetAllUseCaseImpl implements RolGetAllUseCase {

    private final RolRepositoryPort rolRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RolResponse> getAll(RolQuery query) {
        Page<Rol> rolPage = rolRepositoryPort.findAll(query);

        Page<RolResponse> responsePage = rolPage.map(rol -> new RolResponse(
            rol.getId(),
            rol.getNombre(),
            rol.getPermisos().stream().map(p -> new PermisoResponse(p.getId(), p.getNombre())).collect(Collectors.toSet())
        ));

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