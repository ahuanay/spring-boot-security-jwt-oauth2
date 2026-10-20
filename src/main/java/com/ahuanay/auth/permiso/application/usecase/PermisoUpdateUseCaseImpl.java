package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.port.in.PermisoUpdateUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoUpdateUseCaseImpl implements PermisoUpdateUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional
    public PermisoResponse update(Long id, PermisoCommand command) {
        Permiso permiso = new Permiso(id, command.nombre());

        permisoRepositoryPort.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el permiso solicitado", id));

        permisoRepositoryPort.findByNombre(command.nombre())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new PermisoDuplicadoException("El permiso '" + command.nombre() + "' ya existe.");
                }
            });

        Permiso permisoSave = permisoRepositoryPort.save(permiso);

        return new PermisoResponse(permisoSave.getId(), permisoSave.getNombre());
    }
}