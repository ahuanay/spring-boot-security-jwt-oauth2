package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ResetPasswordCommand;
import com.ahuanay.auth.autenticacion.application.port.in.ResetPasswordUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final String MENSAJE_TOKEN_INVALIDO = "El token de recuperación es inválido o expirado.";

    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    @Transactional
    public MensajeResponse resetPassword(ResetPasswordCommand command) {
        if (!ReglaContrasena.esSegura(command.newPassword())) {
            throw new UsuarioInvalidoException(ReglaContrasena.MENSAJE);
        }

        String tokenHash = resetTokenGeneratorPort.hash(command.token());
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepositoryPort.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new CredencialInvalidoException(MENSAJE_TOKEN_INVALIDO);
        }
        PasswordResetToken token = tokenOpt.get();
        if (token.estaUsado()) {
            throw new CredencialInvalidoException(MENSAJE_TOKEN_INVALIDO);
        }
        if (token.estaExpirado(LocalDateTime.now())) {
            passwordResetTokenRepositoryPort.deleteByTokenHash(token.getTokenHash());
            throw new CredencialInvalidoException(MENSAJE_TOKEN_INVALIDO);
        }

        Usuario usuario = usuarioRepositoryPort.findByEmail(token.getEmail())
            .orElseThrow(() -> new CredencialInvalidoException("Usuario no encontrado."));

        String passwordHacheada = passwordHasherPort.encode(command.newPassword());
        Usuario usuarioActualizado = new Usuario(
            usuario.getId(), usuario.getEmail(), passwordHacheada,
            usuario.getProvider(), usuario.getProviderId(), usuario.getActive(),
            usuario.getMfaEnabled(), usuario.getRoles());
        usuarioRepositoryPort.save(usuarioActualizado);

        token.marcarComoUsado(LocalDateTime.now());
        passwordResetTokenRepositoryPort.save(token);

        refreshTokenRepositoryPort.deleteByEmail(usuario.getEmail());

        return new MensajeResponse(
            "La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales.");
    }
}
