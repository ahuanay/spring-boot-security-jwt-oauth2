package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.ReenviarOtpCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface ReenviarOtpUseCase {
    MensajeResponse reenviarOtp(ReenviarOtpCommand command);
}
