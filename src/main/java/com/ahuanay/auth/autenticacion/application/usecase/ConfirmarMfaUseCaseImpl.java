package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ConfirmarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.in.ConfirmarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.MfaNoDisponibleException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfirmarMfaUseCaseImpl implements ConfirmarMfaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Value("${app.security.mfa.max-attempts}")
    private int maxAttempts;

    @Override
    @Transactional
    public MensajeResponse confirmarMfa(ConfirmarMfaCommand command) {
        Usuario usuario = usuarioRepositoryPort.findById(command.usuarioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        if (usuario.getProvider() != AuthProvider.LOCAL) {
            throw new MfaNoDisponibleException(
                "Los usuarios registrados mediante Google/GitHub no pueden activar el doble factor de autenticación.");
        }

        MfaOtp otp = mfaOtpRepositoryPort.findByEmailAndProposito(usuario.getEmail(), PropositoOtp.ACTIVACION)
            .orElseThrow(() -> new OtpInvalidoException(
                "No hay un código de activación pendiente. Inicia la activación nuevamente."));

        if (otp.estaUsado()) {
            throw new OtpInvalidoException("No hay un código de activación pendiente. Inicia la activación nuevamente.");
        }
        if (otp.estaExpirado(LocalDateTime.now())) {
            mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
            throw new OtpInvalidoException("El código de activación expiró. Inicia la activación nuevamente.");
        }

        validarCodigo(otp, command.codigo());
        
        mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
        Usuario usuarioActivado = new Usuario(
            usuario.getId(), usuario.getEmail(), usuario.getPassword(),
            usuario.getProvider(), usuario.getProviderId(), usuario.getActive(),
            true, usuario.getRoles());
        usuarioRepositoryPort.save(usuarioActivado);

        return new MensajeResponse(
            "El doble factor de autenticación quedó activado. El próximo inicio de sesión te pedirá el código de acceso.");
    }

    private void validarCodigo(MfaOtp otp, String codigoPlano) {
        String codigoHash = resetTokenGeneratorPort.hash(codigoPlano);
        if (codigoHash.equals(otp.getCodigoHash())) {
            return;
        }

        otp.registrarIntentoFallido();
        if (otp.getIntentos() >= maxAttempts) {
            mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
            throw new OtpBloqueadoException(
                "Se superó el número máximo de intentos. Inicia la activación nuevamente.");
        }

        mfaOtpRepositoryPort.save(otp);
        throw new OtpInvalidoException(
            "El código de activación es incorrecto. Intentos restantes: " + (maxAttempts - otp.getIntentos()) + ".");
    }
}
