package com.ahuanay.auth.permiso.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.port.in.PermisoDeleteUseCase;
import com.ahuanay.auth.permiso.application.port.out.PermisoRepositoryPort;
import com.ahuanay.auth.permiso.domain.exception.PermisoEnUsoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermisoDeleteUseCaseImpl implements PermisoDeleteUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional
    public void delete(Long id) {
        permisoRepositoryPort.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el permiso solicitado", id));

        List<Long> rolIds = permisoRepositoryPort.findRolIdsByPermisoId(id);
        if (!rolIds.isEmpty()) {
            String rolesTexto = rolIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            throw new PermisoEnUsoException(
                "El permiso no puede eliminarse porque está asignado a los roles con ID: " + rolesTexto);
        }

        permisoRepositoryPort.deleteById(id);
    }
}
