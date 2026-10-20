package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.in.PermisoDeleteUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoDeleteUseCaseImpl implements PermisoDeleteUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional
    public void delete(Long id) {
        permisoRepositoryPort.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el permiso solicitado", id));

        permisoRepositoryPort.deleteById(id);
    }
}