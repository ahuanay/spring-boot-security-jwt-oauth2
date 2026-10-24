package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.ConfirmarMfaCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface ConfirmarMfaUseCase {
    MensajeResponse confirmarMfa(ConfirmarMfaCommand command);
}
