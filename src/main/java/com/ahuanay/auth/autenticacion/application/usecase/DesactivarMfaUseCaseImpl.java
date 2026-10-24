package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.DesactivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.in.DesactivarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordHasherPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesactivarMfaUseCaseImpl implements DesactivarMfaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    @Transactional
    public MensajeResponse desactivarMfa(DesactivarMfaCommand command) {
        Usuario usuario = usuarioRepositoryPort.findById(command.usuarioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        if (usuario.getProvider() != AuthProvider.LOCAL) {
            throw new UsuarioInvalidoException(
                "Los usuarios registrados mediante Google/GitHub no tienen doble factor de autenticación.");
        }
        if (!Boolean.TRUE.equals(usuario.getMfaEnabled())) {
            throw new UsuarioInvalidoException("El doble factor de autenticación no está activado.");
        }
        if (!passwordHasherPort.matches(command.password(), usuario.getPassword())) {
            throw new CredencialInvalidoException("La contraseña del usuario es incorrecta.");
        }

        Usuario usuarioActualizado = new Usuario(
            usuario.getId(), usuario.getEmail(), usuario.getPassword(),
            usuario.getProvider(), usuario.getProviderId(), usuario.getActive(),
            false, usuario.getRoles());
        usuarioRepositoryPort.save(usuarioActualizado);
        
        mfaOtpRepositoryPort.deleteByEmail(usuario.getEmail());

        return new MensajeResponse(
            "El doble factor de autenticación quedó desactivado. Inicia sesión solo con tu email y contraseña.");
    }
}
