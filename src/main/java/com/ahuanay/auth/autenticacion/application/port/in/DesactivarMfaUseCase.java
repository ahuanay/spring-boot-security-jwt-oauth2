package com.ahuanay.auth.autenticacion.application.port.in;

import com.ahuanay.auth.autenticacion.application.command.DesactivarMfaCommand;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;

public interface DesactivarMfaUseCase {
    MensajeResponse desactivarMfa(DesactivarMfaCommand command);
}
