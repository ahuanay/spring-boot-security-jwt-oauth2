package com.ahuanay.auth.autenticacion.infrastructure.adapters.email;

import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
public class LogEmailSenderAdapter implements EmailSenderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEmailSenderAdapter.class);

    @Override
    public void enviarRecuperacionContrasena(String destinatario, String link) {
        LOGGER.info("==========================================================");
        LOGGER.info("[EMAIL de recuperación de contraseña] Para: {}", destinatario);
        LOGGER.info("[EMAIL de recuperación de contraseña] Link: {}", link);
        LOGGER.info("==========================================================");
    }

    @Override
    public void enviarOtpAcceso(String destinatario, String codigo, int minutosValidez) {
        LOGGER.info("==========================================================");
        LOGGER.info("[EMAIL de código de acceso (OTP)] Para: {}", destinatario);
        LOGGER.info("[EMAIL de código de acceso (OTP)] Código: {} (expira en {} minutos)", codigo, minutosValidez);
        LOGGER.info("==========================================================");
    }

    @Override
    public void enviarOtpActivacion(String destinatario, String codigo, int minutosValidez) {
        LOGGER.info("==========================================================");
        LOGGER.info("[EMAIL de activación del doble factor] Para: {}", destinatario);
        LOGGER.info("[EMAIL de activación del doble factor] Código: {} (expira en {} minutos)", codigo, minutosValidez);
        LOGGER.info("==========================================================");
    }
}
