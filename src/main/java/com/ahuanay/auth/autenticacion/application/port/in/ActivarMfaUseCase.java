package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.ActivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface ActivarMfaUseCase {
    MensajeResponse activarMfa(ActivarMfaCommand command);
}
