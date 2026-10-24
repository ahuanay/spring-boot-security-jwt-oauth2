package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.LoginCommand;
import com.ahuanay.auth.autenticacion.application.port.in.LoginUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
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
public class LoginUseCaseImpl implements LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtServicePort jwtServicePort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;
    private final EmailSenderPort emailSenderPort;

    @Value("${app.security.mfa.expiration-minutes}")
    private int otpExpirationMinutes;

    @Override
    @Transactional
    public LoginResponse login(LoginCommand command) {
        Usuario usuario = usuarioRepositoryPort.findByEmail(command.email())
            .orElseThrow(() -> new CredencialInvalidoException("Credenciales inválidas."));

        if (!Boolean.TRUE.equals(usuario.getActive())) {
            throw new CredencialInvalidoException("El usuario se encuentra inactivo.");
        }

        if (!passwordHasherPort.matches(command.password(), usuario.getPassword())) {
            throw new CredencialInvalidoException("Credenciales inválidas.");
        }

        if (Boolean.TRUE.equals(usuario.getMfaEnabled())) {
            return iniciarVerificacionOtp(usuario);
        }

        AuthResponse credenciales = emitirTokens(usuario);
        return LoginResponse.conCredenciales(credenciales);
    }

    private LoginResponse iniciarVerificacionOtp(Usuario usuario) {
        mfaOtpRepositoryPort.deleteByEmailAndProposito(usuario.getEmail(), PropositoOtp.LOGIN);

        String tokenPlano = resetTokenGeneratorPort.generarToken();
        String tokenHash = resetTokenGeneratorPort.hash(tokenPlano);
        String codigoPlano = resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO);
        String codigoHash = resetTokenGeneratorPort.hash(codigoPlano);
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        mfaOtpRepositoryPort.save(new MfaOtp(
            null, tokenHash, usuario.getEmail(), codigoHash, PropositoOtp.LOGIN, expiracion, 0, null));
        emailSenderPort.enviarOtpAcceso(usuario.getEmail(), codigoPlano, otpExpirationMinutes);

        return LoginResponse.conOtpRequerido(tokenPlano, otpExpirationMinutes * 60L);
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
