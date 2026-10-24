package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.mapper;


import com.ahuanay.auth.autenticacion.application.command.*;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthRestMapper {
    LoginCommand toCommand(LoginRestRequest request);

    RegisterCommand toCommand(RegisterRestRequest request);

    RefreshTokenCommand toCommand(RefreshTokenRestRequest request);

    ForgotPasswordCommand toCommand(ForgotPasswordRestRequest request);

    ResetPasswordCommand toCommand(ResetPasswordRestRequest request);

    VerificarOtpCommand toCommand(VerificarOtpRestRequest request);

    ReenviarOtpCommand toCommand(ReenviarOtpRestRequest request);

    ActivarMfaCommand toCommand(ActivarMfaRestRequest request, Long usuarioId);

    ConfirmarMfaCommand toCommand(ConfirmarMfaRestRequest request, Long usuarioId);

    DesactivarMfaCommand toCommand(DesactivarMfaRestRequest request, Long usuarioId);

    AuthRestResponse toRestResponse(AuthResponse response);

    MensajeRestResponse toRestResponse(MensajeResponse response);

    default LoginRestResponse toRestResponse(LoginResponse response) {
        if (Boolean.TRUE.equals(response.mfaRequerido())) {
            return new LoginRestResponse(true, null, null, null, response.tokenMfa(), response.expiraEnSegundos());
        }
        return new LoginRestResponse(
            false,
            response.credenciales().accessToken(),
            response.credenciales().refreshToken(),
            response.credenciales().tokenType(),
            null,
            null
        );
    }
}
