package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.VerificarOtpCommand;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;

public interface VerificarOtpUseCase {
    AuthResponse verificarOtp(VerificarOtpCommand command);
}
