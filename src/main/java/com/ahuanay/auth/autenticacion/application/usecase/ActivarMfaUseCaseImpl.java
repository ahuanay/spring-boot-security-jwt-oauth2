package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ActivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.port.in.ActivarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.MfaNoDisponibleException;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
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
public class ActivarMfaUseCaseImpl implements ActivarMfaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;
    private final EmailSenderPort emailSenderPort;

    @Value("${app.security.mfa.expiration-minutes}")
    private int otpExpirationMinutes;

    @Override
    @Transactional
    public MensajeResponse activarMfa(ActivarMfaCommand command) {
        Usuario usuario = usuarioRepositoryPort.findById(command.usuarioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        if (usuario.getProvider() != AuthProvider.LOCAL) {
            throw new MfaNoDisponibleException(
                "Los usuarios registrados mediante Google/GitHub no requieren doble factor de autenticación.");
        }
        if (Boolean.TRUE.equals(usuario.getMfaEnabled())) {
            throw new UsuarioInvalidoException("El doble factor de autenticación ya se encuentra activado.");
        }
        if (!passwordHasherPort.matches(command.password(), usuario.getPassword())) {
            throw new CredencialInvalidoException("La contraseña del usuario es incorrecta.");
        }

        mfaOtpRepositoryPort.deleteByEmailAndProposito(usuario.getEmail(), PropositoOtp.ACTIVACION);

        String tokenHash = resetTokenGeneratorPort.hash(resetTokenGeneratorPort.generarToken());
        String codigoPlano = resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO);
        String codigoHash = resetTokenGeneratorPort.hash(codigoPlano);
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        mfaOtpRepositoryPort.save(new MfaOtp(
            null, tokenHash, usuario.getEmail(), codigoHash, PropositoOtp.ACTIVACION, expiracion, 0, null));
        emailSenderPort.enviarOtpActivacion(usuario.getEmail(), codigoPlano, otpExpirationMinutes);

        return new MensajeResponse(
            "Enviamos un código de activación a tu correo. Introdúcelo para finalizar la activación del doble factor.");
    }
}
