package com.ahuanay.auth.rol.application.usecase;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.rol.application.port.in.RolDeleteUseCase;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolDeleteUseCaseImpl implements RolDeleteUseCase {

    private final RolRepositoryPort rolRepositoryPort;

    @Override
    @Transactional
    public void delete(Long id) {
        rolRepositoryPort.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol solicitado", id));

        rolRepositoryPort.deleteById(id);
    }
}