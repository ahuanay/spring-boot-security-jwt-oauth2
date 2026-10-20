package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.rol.application.port.in.RolGetByIdUseCase;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolGetByIdUseCaseImpl implements RolGetByIdUseCase {

    private final RolRepositoryPort rolRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public RolResponse getById(Long id) {
        Rol rol = rolRepositoryPort.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol solicitado", id));

        return new RolResponse(
            rol.getId(),
            rol.getNombre(),
            rol.getPermisos().stream().map(p -> new PermisoResponse(p.getId(), p.getNombre())).collect(Collectors.toSet())
        );
    }
}