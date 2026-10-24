package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.RegisterCommand;
import com.ahuanay.auth.autenticacion.application.port.in.RegisterUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordHasherPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.UsuarioResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioDuplicadoException;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RolRepositoryPort rolRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    @Transactional
    public UsuarioResponse register(RegisterCommand command) {
        Usuario.validarReglasCreacion(command.email(), command.password(), AuthProvider.LOCAL, command.roleIds());

        if (usuarioRepositoryPort.findByEmail(command.email()).isPresent()) {
            throw new UsuarioDuplicadoException("Ya existe un usuario registrado con el email: " + command.email());
        }

        Set<Rol> roles = new HashSet<>(rolRepositoryPort.findAllByIdIn(command.roleIds()));

        Set<Long> idsEncontrados = roles.stream()
            .map(Rol::getId)
            .collect(Collectors.toSet());

        Set<Long> idsNoEncontrados = command.roleIds().stream()
            .filter(roleId -> !idsEncontrados.contains(roleId))
            .collect(Collectors.toSet());

        if (!idsNoEncontrados.isEmpty()) {
            throw new RecursoNoEncontradoException(
                "Uno o más roles proporcionados no existen.", idsNoEncontrados
            );
        }

        String encodedPassword = passwordHasherPort.encode(command.password());

        Usuario usuario = new Usuario(
            null,
            command.email(),
            encodedPassword,
            AuthProvider.LOCAL,
            null,
            true,
            false,
            roles
        );

        Usuario savedUsuario = usuarioRepositoryPort.save(usuario);

        Set<RolResponse> rolResponses = savedUsuario.getRoles().stream().map(rol ->
            new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getPermisos().stream().map(p -> new PermisoResponse(p.getId(), p.getNombre())).collect(Collectors.toSet())
            )
        ).collect(Collectors.toSet());

        return new UsuarioResponse(savedUsuario.getId(), savedUsuario.getEmail(), savedUsuario.getActive(), savedUsuario.getMfaEnabled(), rolResponses);
    }
}