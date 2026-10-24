package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ForgotPasswordCommand;
import com.ahuanay.auth.autenticacion.application.port.in.ForgotPasswordUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import com.ahuanay.auth.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.model.PasswordResetToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCaseImpl implements ForgotPasswordUseCase {

    private static final String MENSAJE_GENERICO =
        "Si el correo está registrado, recibirás un enlace para restablecer la contraseña.";

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;
    private final EmailSenderPort emailSenderPort;

    @Value("${app.security.password-reset.expiration-minutes}")
    private int expirationMinutes;

    @Value("${app.security.password-reset.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public MensajeResponse forgotPassword(ForgotPasswordCommand command) {
        Optional<Usuario> usuarioOpt = usuarioRepositoryPort.findByEmail(command.email());

        if (usuarioOpt.isEmpty()) {
            return new MensajeResponse(MENSAJE_GENERICO);
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getProvider() != AuthProvider.LOCAL) {
            return new MensajeResponse(obtenerMensajeOAuth2(usuario.getProvider()));
        }

        passwordResetTokenRepositoryPort.deleteByEmail(usuario.getEmail());

        String tokenPlano = resetTokenGeneratorPort.generarToken();
        String tokenHash = resetTokenGeneratorPort.hash(tokenPlano);
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(expirationMinutes);
        passwordResetTokenRepositoryPort.save(new PasswordResetToken(
            null, tokenHash, usuario.getEmail(), expiracion, null));

        String link = frontendUrl + "?token=" + tokenPlano;
        emailSenderPort.enviarRecuperacionContrasena(usuario.getEmail(), link);

        return new MensajeResponse(MENSAJE_GENERICO);
    }

    private String obtenerMensajeOAuth2(AuthProvider provider) {
        String nombre = switch (provider) {
            case GOOGLE -> "Google";
            case GITHUB -> "GitHub";
            case LOCAL -> "nuestra aplicación";
        };
        return "Este correo está registrado mediante " + nombre
            + ". Inicia sesión con ese proveedor; el cambio de contraseña se realiza desde tu cuenta "
            + nombre + ".";
    }
}
