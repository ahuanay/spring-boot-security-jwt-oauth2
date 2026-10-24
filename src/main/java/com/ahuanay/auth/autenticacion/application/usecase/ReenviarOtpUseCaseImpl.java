package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.ReenviarOtpCommand;
import com.ahuanay.auth.autenticacion.application.port.in.ReenviarOtpUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import com.ahuanay.auth.autenticacion.application.port.out.MfaOtpRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.ResetTokenGeneratorPort;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReenviarOtpUseCaseImpl implements ReenviarOtpUseCase {

    private static final String MENSAJE_TOKEN_INVALIDO =
        "El token de verificación es inválido, el código expiró o ya fue utilizado.";

    private final MfaOtpRepositoryPort mfaOtpRepositoryPort;
    private final ResetTokenGeneratorPort resetTokenGeneratorPort;
    private final EmailSenderPort emailSenderPort;

    @Value("${app.security.mfa.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${app.security.mfa.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    @Override
    @Transactional
    public MensajeResponse reenviarOtp(ReenviarOtpCommand command) {
        String tokenHash = resetTokenGeneratorPort.hash(command.tokenMfa());
        MfaOtp otp = mfaOtpRepositoryPort.findByTokenHash(tokenHash)
            .orElseThrow(() -> new CredencialInvalidoException(MENSAJE_TOKEN_INVALIDO));

        if (otp.estaUsado() || otp.estaExpirado(LocalDateTime.now())) {
            mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
            throw new CredencialInvalidoException(MENSAJE_TOKEN_INVALIDO);
        }

        validarCooldownReenvio(otp);

        mfaOtpRepositoryPort.deleteByTokenHash(otp.getTokenHash());
        String codigoPlano = resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO);
        String codigoHash = resetTokenGeneratorPort.hash(codigoPlano);
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        mfaOtpRepositoryPort.save(new MfaOtp(
            null, otp.getTokenHash(), otp.getEmail(), codigoHash, PropositoOtp.LOGIN, expiracion, 0, null));
        emailSenderPort.enviarOtpAcceso(otp.getEmail(), codigoPlano, otpExpirationMinutes);

        return new MensajeResponse("Generamos un nuevo código y lo enviamos a tu correo.");
    }

    private void validarCooldownReenvio(MfaOtp otp) {
        LocalDateTime emitidoAt = otp.getExpiresAt().minusMinutes(otpExpirationMinutes);
        if (LocalDateTime.now().isBefore(emitidoAt.plusSeconds(resendCooldownSeconds))) {
            throw new OtpBloqueadoException(
                "Debes esperar " + resendCooldownSeconds + " segundos para solicitar un nuevo código.");
        }
    }
}
