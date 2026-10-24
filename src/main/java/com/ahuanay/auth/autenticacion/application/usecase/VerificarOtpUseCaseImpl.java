package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.VerificarOtpCommand;
import com.ahuanay.auth.autenticacion.application.port.in.VerificarOtpUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificarOtpUseCaseImpl implements VerificarOtpUseCase {

    private static final String MENSAJE_CODIGO_INVALIDO = "El token de verificación es inválido, el código expiró o ya fue utilizado.";

    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final JwtServicePort jwtServicePort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Value("${app.security.mfa.max-attempts}")
    private int maxAttempts;

    @Override
    @Transactional
    public AuthResponse verificarOtp(VerificarOtpCommand command) {
        String tokenHash = resetTokenGeneratorPort.hash(command.tokenMfa());
        MfaOtp otp = mfaOtpRepositoryPort.findByTokenHash(tokenHash)
            .orElseThrow(() -> new CredencialInvalidoException(MENSAJE_CODIGO_INVALIDO));

        if (otp.estaUsado() || otp.getProposito() != PropositoOtp.LOGIN) {
            throw new CredencialInvalidoException(MENSAJE_CODIGO_INVALIDO);
        }
        if (otp.estaExpirado(LocalDateTime.now())) {
            mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
            throw new CredencialInvalidoException(MENSAJE_CODIGO_INVALIDO);
        }

        validarCodigo(otp, command.codigo());

        Usuario usuario = usuarioRepositoryPort.findByEmail(otp.getEmail())
            .orElseThrow(() -> new CredencialInvalidoException("Usuario no encontrado."));
        if (!Boolean.TRUE.equals(usuario.getActive())) {
            throw new CredencialInvalidoException("El usuario se encuentra inactivo.");
        }

        otp.marcarComoUsado(LocalDateTime.now());
        mfaOtpRepositoryPort.save(otp);

        return emitirTokens(usuario);
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
                "Se superó el número máximo de intentos. Inicia sesión nuevamente para recibir un nuevo código.");
        }

        mfaOtpRepositoryPort.save(otp);
        throw new OtpInvalidoException(
            "El código es incorrecto. Intentos restantes: " + (maxAttempts - otp.getIntentos()) + ".");
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        String accessToken = jwtServicePort.generateAccessToken(usuario);
        RefreshTokenGenerado refreshTokenGenerado = jwtServicePort.generateRefreshToken(usuario);

        refreshTokenRepositoryPort.deleteByEmail(usuario.getEmail());
        refreshTokenRepositoryPort.save(new RefreshToken(
            null, refreshTokenGenerado.token(), usuario.getEmail(), refreshTokenGenerado.expiresAt()));

        return new AuthResponse(accessToken, refreshTokenGenerado.token());
    }
}
