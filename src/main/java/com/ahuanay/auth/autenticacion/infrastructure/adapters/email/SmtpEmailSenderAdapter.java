package com.ahuanay.auth.autenticacion.infrastructure.adapters.email;

import com.ahuanay.auth.autenticacion.application.port.out.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final String ASUNTO_RECUPERACION = "Recuperación de contraseña";
    private static final String ASUNTO_OTP_ACCESO = "Código de acceso (OTP)";
    private static final String ASUNTO_OTP_ACTIVACION = "Activación del doble factor de autenticación";

    private final JavaMailSender mailSender;
    private final String remitente;

    @Value("${app.security.password-reset.expiration-minutes:30}")
    private int tokenExpirationTimeInMinutes;

    public SmtpEmailSenderAdapter(
        JavaMailSender mailSender,
        @Value("${spring.mail.username:}") String remitente
    ) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarRecuperacionContrasena(String destinatario, String link) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (remitente != null && !remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject(ASUNTO_RECUPERACION);
        mensaje.setText("""
                Hola:
            
                Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                Abre el siguiente enlace para continuar (el enlace expira en %s minutos
                y solo puede utilizarse una vez):
            
                %s
            
                Si tú no solicitaste este cambio, ignora este mensaje y tu contraseña
                seguirá siendo la misma.
            """.formatted(tokenExpirationTimeInMinutes, link));
        mailSender.send(mensaje);
    }

    @Override
    public void enviarOtpAcceso(String destinatario, String codigo, int minutosValidez) {
        enviarOtp(destinatario, codigo, minutosValidez, ASUNTO_OTP_ACCESO, """
                Hola:
            
                Tu código de acceso de doble factor es:
            
                %s
            
                El código expira en %s minutos y solo puede utilizarse una vez.
                Si tú no intentaste iniciar sesión, ignora este mensaje y tu cuenta
                seguirá protegida.
            """);
    }

    @Override
    public void enviarOtpActivacion(String destinatario, String codigo, int minutosValidez) {
        enviarOtp(destinatario, codigo, minutosValidez, ASUNTO_OTP_ACTIVACION, """
                Hola:
            
                Introduce el siguiente código en la aplicación para activar el doble
                factor de autenticación de tu cuenta:
            
                %s
            
                El código expira en %s minutos y solo puede utilizarse una vez.
                Si tú no solicitaste esta activación, ignora este mensaje y tu cuenta
                seguirá configurada tal como está.
            """);
    }

    private void enviarOtp(
        String destinatario, String codigo, int minutosValidez, String asunto, String plantilla
    ) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (remitente != null && !remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(plantilla.formatted(codigo, minutosValidez));
        mailSender.send(mensaje);
    }
}
