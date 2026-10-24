package com.ahuanay.auth.autenticacion.application.port.out;

public interface ResetTokenGeneratorPort {
    String generarToken();

    String generarCodigo(int longitud);

    String hash(String token);
}
