package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.ForgotPasswordCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface ForgotPasswordUseCase {
    MensajeResponse forgotPassword(ForgotPasswordCommand command);
}
