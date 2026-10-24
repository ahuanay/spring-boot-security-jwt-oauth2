package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.autenticacion.application.port.in.ActivarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.ConfirmarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.DesactivarMfaUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.ForgotPasswordUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.LoginUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.ReenviarOtpUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.RefreshTokenUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.RegisterUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.ResetPasswordUseCase;
import com.ahuanay.auth.autenticacion.application.port.in.VerificarOtpUseCase;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.application.response.UsuarioResponse;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.ActivarMfaRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.AuthRestResponse;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.ConfirmarMfaRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.DesactivarMfaRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.ForgotPasswordRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.LoginRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.LoginRestResponse;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.MensajeRestResponse;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.ReenviarOtpRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.RefreshTokenRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.RegisterRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.ResetPasswordRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.VerificarOtpRestRequest;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.mapper.AuthRestMapper;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión, doble factor (2FA por OTP de email) y renovación de tokens")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final VerificarOtpUseCase verificarOtpUseCase;
    private final ReenviarOtpUseCase reenviarOtpUseCase;
    private final ActivarMfaUseCase activarMfaUseCase;
    private final ConfirmarMfaUseCase confirmarMfaUseCase;
    private final DesactivarMfaUseCase desactivarMfaUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    private final AuthRestMapper authRestMapper;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión",
        description = "Autentica al usuario mediante email y contraseña. Si el usuario tiene el doble factor "
            + "activado (mfaEnabled), no entrega tokens: responde mfaRequerido=true con el tokenMfa para verificar "
            + "el código OTP enviado a su email (mediante /otp/verificar); en caso contrario entrega los tokens JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa: acceso directo con tokens JWT "
            + "o desafío de doble factor (mfaRequerido)"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginRestResponse> login(@Valid @RequestBody LoginRestRequest request) {
        LoginResponse response = loginUseCase.login(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/otp/verificar")
    @SecurityRequirements
    @Operation(summary = "Verificar el segundo factor (OTP)",
        description = "Verifica el código OTP de 6 dígitos enviado al email del usuario, usando el token intermedio "
            + "entregado por el login. Si el código es correcto entrega los tokens JWT. La verificación fallida "
            + "consume intentos: al agotar el máximo, el desafío se cancela y hay que iniciar sesión de nuevo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código correcto: se retornan Access Token y Refresh Token",
            content = @Content(schema = @Schema(implementation = AuthRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token intermedio inválido o expirado, o código incorrecto",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Se superó el número máximo de intentos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthRestResponse> verificarOtp(@Valid @RequestBody VerificarOtpRestRequest request) {
        AuthResponse response = verificarOtpUseCase.verificarOtp(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/otp/reenviar")
    @SecurityRequirements
    @Operation(summary = "Reenviar el código OTP",
        description = "Genera y envía un nuevo código OTP para el desafío de doble factor en curso, respetando un "
            + "tiempo de espera entre reenvíos. El token intermedio sigue siendo válido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nuevo código generado y enviado al email"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token intermedio inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "El reenvío/desafío aún está bloqueado por el tiempo de espera",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> reenviarOtp(@Valid @RequestBody ReenviarOtpRestRequest request) {
        MensajeResponse response = reenviarOtpUseCase.reenviarOtp(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/mfa/activar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Iniciar la activación del doble factor",
        description = "Envía un código de activación al email del usuario autenticado tras validar su contraseña. "
            + "El doble factor solo se activa al confirmar el código con /mfa/confirmar. "
            + "No está disponible para usuarios registrados mediante Google/GitHub.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Código de activación generado y enviado al email"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Contraseña incorrecta o token de acceso inválido/ausente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El doble factor no está disponible para el usuario "
            + "(proveedor externo) o ya se encuentra activado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> activarMfa(
        @AuthenticationPrincipal Jwt principal,
        @Valid @RequestBody ActivarMfaRestRequest request
    ) {
        MensajeResponse response = activarMfaUseCase
            .activarMfa(authRestMapper.toCommand(request, Long.valueOf(principal.getClaimAsString("userId"))));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/mfa/confirmar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmar la activación del doble factor",
        description = "Activa el doble factor de autenticación del usuario autenticado si el código recibido "
            + "por email es correcto. La verificación fallida consume intentos; al agotar el máximo hay que "
            + "solicitar la activación nuevamente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doble factor activado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Código incorrecto, expirado o sin desafío pendiente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El doble factor no está disponible para el usuario "
            + "(proveedor externo)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Se superó el número máximo de intentos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> confirmarMfa(
        @AuthenticationPrincipal Jwt principal,
        @Valid @RequestBody ConfirmarMfaRestRequest request
    ) {
        MensajeResponse response = confirmarMfaUseCase
            .confirmarMfa(authRestMapper.toCommand(request, Long.valueOf(principal.getClaimAsString("userId"))));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @DeleteMapping("/mfa")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desactivar el doble factor",
        description = "Desactiva el doble factor de autenticación del usuario autenticado tras validar su password "
            + "y cancela cualquier desafío OTP pendiente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doble factor desactivado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Contraseña incorrecta o token de acceso inválido/ausente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El doble factor no está activado para el usuario",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> desactivarMfa(
        @AuthenticationPrincipal Jwt principal,
        @Valid @RequestBody DesactivarMfaRestRequest request
    ) {
        MensajeResponse response = desactivarMfaUseCase
            .desactivarMfa(authRestMapper.toCommand(request, Long.valueOf(principal.getClaimAsString("userId"))));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema con roles asignados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Uno o más roles proporcionados no existen",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Ya existe un usuario registrado con el email proporcionado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRestRequest request) {
        UsuarioResponse response = registerUseCase.register(authRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Renovar Token", description = "Genera un nuevo Access Token y Refresh Token a partir de un Refresh Token válido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens renovados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthRestResponse> refreshToken(@Valid @RequestBody RefreshTokenRestRequest request) {
        AuthResponse response = refreshTokenUseCase.refreshToken(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements
    @Operation(summary = "Solicitar recuperación de contraseña",
        description = "Genera un token de recuperación temporal para el usuario local y lo envía por email. "
            + "Si el email no existe o pertenece a un usuario de un proveedor externo, se responde con un "
            + "mensaje informativo sin credenciales ni tokens.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud procesada con mensaje informativo"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRestRequest request) {
        MensajeResponse response = forgotPasswordUseCase.forgotPassword(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }

    @PostMapping("/reset-password")
    @SecurityRequirements
    @Operation(summary = "Restablecer contraseña",
        description = "Restablece la contraseña del usuario local mediante un token de recuperación válido, "
            + "lo marca como utilizado y revoca las sesiones activas del usuario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token de recuperación inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MensajeRestResponse> resetPassword(@Valid @RequestBody ResetPasswordRestRequest request) {
        MensajeResponse response = resetPasswordUseCase.resetPassword(authRestMapper.toCommand(request));
        return ResponseEntity.ok(authRestMapper.toRestResponse(response));
    }
}
