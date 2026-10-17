package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.in.PermisoGetByIdUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoGetByIdUseCaseImpl implements PermisoGetByIdUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PermisoResponse getById(Long id) {
        return permisoRepositoryPort.findById(id)
            .map(permiso -> new PermisoResponse(permiso.getId(), permiso.getNombre()))
            .orElseThrow(() -> new RecursoNoEncontradoException("Permiso", id));
    }
}