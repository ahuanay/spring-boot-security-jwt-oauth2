package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Confirmación de la activación del doble factor con el código OTP recibido por email")
public record ConfirmarMfaRestRequest(

    @Schema(description = "Código OTP de 6 dígitos enviado al email del usuario", example = "482915")
    @NotNull(message = "El código OTP es obligatorio.")
    @Pattern(regexp = "\\d{6}", message = "El código OTP debe tener exactamente 6 dígitos.")
    String codigo
) {
}
