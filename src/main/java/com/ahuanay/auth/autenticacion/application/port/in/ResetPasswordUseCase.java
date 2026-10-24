package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.ResetPasswordCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface ResetPasswordUseCase {
    MensajeResponse resetPassword(ResetPasswordCommand command);
}
