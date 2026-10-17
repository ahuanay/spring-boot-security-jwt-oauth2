package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.port.in.PermisoCreateUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoCreateUseCaseImpl implements PermisoCreateUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional
    public PermisoResponse create(PermisoCommand command) {
        Permiso permiso = new Permiso(null, command.nombre());

        if (permisoRepositoryPort.findByNombre(command.nombre()).isPresent()) {
            throw new PermisoDuplicadoException("El permiso '" + command.nombre() + "' ya existe.");
        }

        Permiso permisoSave = permisoRepositoryPort.save(permiso);

        return new PermisoResponse(permisoSave.getId(), permisoSave.getNombre());
    }
}