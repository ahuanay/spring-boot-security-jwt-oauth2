package com.ahuanay.auth.autenticacion.application.port.out;

public interface EmailSenderPort {
    void enviarRecuperacionContrasena(String destinatario, String link);

    void enviarOtpAcceso(String destinatario, String codigo, int minutosValidez);

    void enviarOtpActivacion(String destinatario, String codigo, int minutosValidez);
}
