package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con mensaje informativo de los flujos de recuperación y restablecimiento de contraseña")
public record MensajeRestResponse(
    @Schema(description = "Mensaje informativo del resultado de la operación",
        example = "Si el correo está registrado, recibirás un enlace para restablecer la contraseña.")
    String mensaje
) {
}
