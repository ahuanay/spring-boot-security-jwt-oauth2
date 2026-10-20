package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.port.in.RolCreateUseCase;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.exception.RolDuplicadoException;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolCreateUseCaseImpl implements RolCreateUseCase {

    private final RolRepositoryPort rolRepositoryPort;
    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional
    public RolResponse create(RolCommand command) {
        Rol.validarReglasCreacion(command.nombre(), command.permisoIds());

        if (rolRepositoryPort.findByNombre(command.nombre()).isPresent()) {
            throw new RolDuplicadoException("El rol '" + command.nombre() + "' ya existe.");
        }

        Set<Permiso> permisos = new HashSet<>(permisoRepositoryPort.findAllByIdIn(command.permisoIds()));

        Set<Long> idsEncontrados = permisos.stream()
            .map(Permiso::getId)
            .collect(Collectors.toSet());

        Set<Long> idsNoEncontrados = command.permisoIds().stream()
            .filter(permisoId -> !idsEncontrados.contains(permisoId))
            .collect(Collectors.toSet());

        if (!idsNoEncontrados.isEmpty()) {
            throw new RecursoNoEncontradoException(
                "Uno o más permisos proporcionados no existen.", idsNoEncontrados
            );
        }

        Rol rol = new Rol(null, command.nombre(), permisos);

        Rol savedRol = rolRepositoryPort.save(rol);

        Set<PermisoResponse> permisoResponses = savedRol.getPermisos().stream()
            .map(p -> new PermisoResponse(p.getId(), p.getNombre()))
            .collect(Collectors.toSet());

        return new RolResponse(savedRol.getId(), savedRol.getNombre(), permisoResponses);
    }
}