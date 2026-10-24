package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.autenticacion.application.command.*;
import com.ahuanay.auth.autenticacion.application.port.in.*;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;
import com.ahuanay.auth.autenticacion.application.response.MensajeResponse;
import com.ahuanay.auth.autenticacion.application.response.UsuarioResponse;
import com.ahuanay.auth.autenticacion.domain.exception.*;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.dto.*;
import com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.mapper.AuthRestMapper;
import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception.GlobalExceptionHandler;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static com.ahuanay.auth.support.MockMvcTestUtils.deleteJson;
import static com.ahuanay.auth.support.MockMvcTestUtils.postJson;
import static com.ahuanay.auth.support.MockMvcTestUtils.usuarioAutenticado;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "SecurePassword123*";
    private static final String REFRESH_TOKEN = "old-refresh-token";
    private static final String ACCESS_TOKEN = "access-token-123";
    private static final String NEW_REFRESH_TOKEN = "refresh-token-456";
    private static final String RESET_TOKEN = "3f9a1c0b7d2e5f8a4c6b9d0e1f2a3b4c5d6e7f809a1b2c3d4e5f60718293a4b5";
    private static final String MENSAJE_GENERICO =
        "Si el correo está registrado, recibirás un enlace para restablecer la contraseña.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private VerificarOtpUseCase verificarOtpUseCase;

    @MockitoBean
    private ReenviarOtpUseCase reenviarOtpUseCase;

    @MockitoBean
    private ActivarMfaUseCase activarMfaUseCase;

    @MockitoBean
    private ConfirmarMfaUseCase confirmarMfaUseCase;

    @MockitoBean
    private DesactivarMfaUseCase desactivarMfaUseCase;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @MockitoBean
    private AuthRestMapper authRestMapper;

    private LoginRestRequest loginRequest;
    private RegisterRestRequest registerRequest;
    private RefreshTokenRestRequest refreshRequest;
    private ForgotPasswordRestRequest forgotRequest;
    private ResetPasswordRestRequest resetRequest;
    private AuthResponse authResponse;
    private AuthRestResponse authRestResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRestRequest(EMAIL, PASSWORD);
        registerRequest = new RegisterRestRequest(EMAIL, PASSWORD, Set.of(2L));
        refreshRequest = new RefreshTokenRestRequest(REFRESH_TOKEN);
        forgotRequest = new ForgotPasswordRestRequest(EMAIL);
        resetRequest = new ResetPasswordRestRequest(RESET_TOKEN, PASSWORD);
        authResponse = new AuthResponse(ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        authRestResponse = AuthRestResponse.builder()
            .accessToken(ACCESS_TOKEN)
            .refreshToken(NEW_REFRESH_TOKEN)
            .tokenType("Bearer")
            .build();
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/login - Debería autenticar exitosamente con tokens cuando el usuario no requiere 2FA")
    void deberiaAutenticarExitosamente_CuandoLasCredencialesSonValidas() throws Exception {
        // Arrange
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        LoginResponse loginResponse = LoginResponse.conCredenciales(authResponse);
        LoginRestResponse loginRestResponse = new LoginRestResponse(
            false, ACCESS_TOKEN, NEW_REFRESH_TOKEN, "Bearer", null, null);
        when(authRestMapper.toCommand(loginRequest)).thenReturn(command);
        when(loginUseCase.login(command)).thenReturn(loginResponse);
        when(authRestMapper.toRestResponse(loginResponse)).thenReturn(loginRestResponse);

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/login", loginRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mfaRequerido").value(false))
            .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
            .andExpect(jsonPath("$.refreshToken").value(NEW_REFRESH_TOKEN))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(loginUseCase, times(1)).login(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debería responder mfaRequerido con tokenMfa cuando el usuario tiene 2FA activado")
    void deberiaResponderMfaRequerido_CuandoElUsuarioTiene2FAActivado() throws Exception {
        // Arrange
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        LoginResponse loginResponse = LoginResponse.conOtpRequerido("mfa-token-789", 300L);
        LoginRestResponse loginRestResponse = new LoginRestResponse(
            true, null, null, null, "mfa-token-789", 300L);
        when(authRestMapper.toCommand(loginRequest)).thenReturn(command);
        when(loginUseCase.login(command)).thenReturn(loginResponse);
        when(authRestMapper.toRestResponse(loginResponse)).thenReturn(loginRestResponse);

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/login", loginRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mfaRequerido").value(true))
            .andExpect(jsonPath("$.tokenMfa").value("mfa-token-789"))
            .andExpect(jsonPath("$.expiraEnSegundos").value(300))
            .andExpect(jsonPath("$.accessToken").doesNotExist());

        verify(loginUseCase, times(1)).login(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestEnLogin_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        LoginRestRequest requestInvalido = new LoginRestRequest("", "");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/login", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, loginUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debería retornar 401 Unauthorized cuando las credenciales son inválidas")
    void deberiaRetornarUnauthorized_CuandoLasCredencialesSonInvalidas() throws Exception {
        // Arrange
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        when(authRestMapper.toCommand(loginRequest)).thenReturn(command);
        when(loginUseCase.login(command))
            .thenThrow(new CredencialInvalidoException("Credenciales inválidas."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/login", loginRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Credenciales inválidas."));

        verify(loginUseCase, times(1)).login(command);
    }

    // ==========================================
    // OTP: VERIFICAR Y REENVIAR
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/otp/verificar - Debería retornar tokens JWT cuando el código OTP es correcto")
    void deberiaRetornarTokensJwt_CuandoElCodigoOtpEsCorrecto() throws Exception {
        // Arrange
        VerificarOtpRestRequest otpRequest = new VerificarOtpRestRequest("mfa-token-789", "482915");
        VerificarOtpCommand command = new VerificarOtpCommand("mfa-token-789", "482915");
        when(authRestMapper.toCommand(otpRequest)).thenReturn(command);
        when(verificarOtpUseCase.verificarOtp(command)).thenReturn(authResponse);
        when(authRestMapper.toRestResponse(authResponse)).thenReturn(authRestResponse);

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/verificar", otpRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
            .andExpect(jsonPath("$.refreshToken").value(NEW_REFRESH_TOKEN))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(verificarOtpUseCase, times(1)).verificarOtp(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verificar - Debería retornar 400 Bad Request cuando el código no tiene 6 dígitos")
    void deberiaRetornarBadRequest_CuandoElCodigoNoTiene6Digitos() throws Exception {
        // Arrange
        VerificarOtpRestRequest requestInvalido = new VerificarOtpRestRequest("mfa-token-789", "48a9");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/verificar", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, verificarOtpUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verificar - Debería retornar 401 Unauthorized cuando el código es incorrecto")
    void deberiaRetornarUnauthorized_CuandoElCodigoOtpEsIncorrecto() throws Exception {
        // Arrange
        VerificarOtpRestRequest otpRequest = new VerificarOtpRestRequest("mfa-token-789", "111111");
        VerificarOtpCommand command = new VerificarOtpCommand("mfa-token-789", "111111");
        when(authRestMapper.toCommand(otpRequest)).thenReturn(command);
        when(verificarOtpUseCase.verificarOtp(command))
            .thenThrow(new OtpInvalidoException("El código es incorrecto. Intentos restantes: 2."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/verificar", otpRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("El código es incorrecto. Intentos restantes: 2."));

        verify(verificarOtpUseCase, times(1)).verificarOtp(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verificar - Debería retornar 429 Too Many Requests cuando se agotaron los intentos")
    void deberiaRetornarTooManyRequests_CuandoSeAgotaronLosIntentos() throws Exception {
        // Arrange
        VerificarOtpRestRequest otpRequest = new VerificarOtpRestRequest("mfa-token-789", "482915");
        VerificarOtpCommand command = new VerificarOtpCommand("mfa-token-789", "482915");
        when(authRestMapper.toCommand(otpRequest)).thenReturn(command);
        when(verificarOtpUseCase.verificarOtp(command))
            .thenThrow(new OtpBloqueadoException("Se superó el número máximo de intentos. "
                + "Inicia sesión nuevamente para recibir un nuevo código."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/verificar", otpRequest)
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.status").value(429));

        verify(verificarOtpUseCase, times(1)).verificarOtp(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/reenviar - Debería responder 200 con el mensaje del nuevo código")
    void deberiaResponder200_CuandoSeReenviaElCodigoOtp() throws Exception {
        // Arrange
        ReenviarOtpRestRequest reenvioRequest = new ReenviarOtpRestRequest("mfa-token-789");
        ReenviarOtpCommand command = new ReenviarOtpCommand("mfa-token-789");
        MensajeResponse mensajeResponse = new MensajeResponse("Generamos un nuevo código y lo enviamos a tu correo.");
        when(authRestMapper.toCommand(reenvioRequest)).thenReturn(command);
        when(reenviarOtpUseCase.reenviarOtp(command)).thenReturn(mensajeResponse);
        when(authRestMapper.toRestResponse(mensajeResponse)).thenReturn(new MensajeRestResponse(mensajeResponse.mensaje()));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/reenviar", reenvioRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Generamos un nuevo código y lo enviamos a tu correo."));

        verify(reenviarOtpUseCase, times(1)).reenviarOtp(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/reenviar - Debería retornar 429 Too Many Requests cuando sigue dentro del tiempo de espera")
    void deberiaRetornarTooManyRequests_CuandoElReenvioSigueDentroDelTiempoDeEspera() throws Exception {
        // Arrange
        ReenviarOtpRestRequest reenvioRequest = new ReenviarOtpRestRequest("mfa-token-789");
        ReenviarOtpCommand command = new ReenviarOtpCommand("mfa-token-789");
        when(authRestMapper.toCommand(reenvioRequest)).thenReturn(command);
        when(reenviarOtpUseCase.reenviarOtp(command))
            .thenThrow(new OtpBloqueadoException("Debes esperar 60 segundos para solicitar un nuevo código."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/otp/reenviar", reenvioRequest)
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.status").value(429))
            .andExpect(jsonPath("$.message").value("Debes esperar 60 segundos para solicitar un nuevo código."));

        verify(reenviarOtpUseCase, times(1)).reenviarOtp(command);
    }

    // ==========================================
    // MFA: ACTIVAR, CONFIRMAR Y DESACTIVAR
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/mfa/activar - Debería responder 200 con el mensaje de código enviado")
    void deberiaResponder200_CuandoSeSolicitaLaActivacionDelDobleFactor() throws Exception {
        // Arrange
        ActivarMfaRestRequest activarRequest = ActivarMfaRestRequest.builder().password(PASSWORD).build();
        ActivarMfaCommand command = new ActivarMfaCommand(1L, PASSWORD);
        MensajeResponse mensajeResponse = new MensajeResponse(
            "Enviamos un código de activación a tu correo. Introdúcelo para finalizar la activación del doble factor.");
        when(authRestMapper.toCommand(activarRequest, 1L)).thenReturn(command);
        when(activarMfaUseCase.activarMfa(command)).thenReturn(mensajeResponse);
        when(authRestMapper.toRestResponse(mensajeResponse)).thenReturn(new MensajeRestResponse(mensajeResponse.mensaje()));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/mfa/activar", activarRequest, usuarioAutenticado(1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value(
                "Enviamos un código de activación a tu correo. Introdúcelo para finalizar la activación del doble factor."));

        verify(activarMfaUseCase, times(1)).activarMfa(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/activar - Debería retornar 409 Conflict cuando el usuario es de un proveedor externo")
    void deberiaRetornarConflict_CuandoElUsuarioEsDeUnProveedorExternoEnActivacion() throws Exception {
        // Arrange
        ActivarMfaRestRequest activarRequest = ActivarMfaRestRequest.builder().password(PASSWORD).build();
        ActivarMfaCommand command = new ActivarMfaCommand(1L, PASSWORD);
        when(authRestMapper.toCommand(activarRequest, 1L)).thenReturn(command);
        when(activarMfaUseCase.activarMfa(command))
            .thenThrow(new MfaNoDisponibleException(
                "Los usuarios registrados mediante Google/GitHub no requieren doble factor de autenticación."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/mfa/activar", activarRequest, usuarioAutenticado(1L))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));

        verify(activarMfaUseCase, times(1)).activarMfa(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/activar - Debería retornar 400 Bad Request cuando la contraseña está vacía")
    void deberiaRetornarBadRequest_CuandoLaContrasenaEstaVaciaEnActivacion() throws Exception {
        // Arrange
        ActivarMfaRestRequest requestInvalido = ActivarMfaRestRequest.builder().password("").build();

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/mfa/activar", requestInvalido, usuarioAutenticado(1L))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, activarMfaUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/confirmar - Debería activar el doble factor y responder el mensaje de confirmación")
    void deberiaActivarElDobleFactor_CuandoElCodigoDeActivacionEsCorrecto() throws Exception {
        // Arrange
        ConfirmarMfaRestRequest confirmarRequest = new ConfirmarMfaRestRequest("482915");
        ConfirmarMfaCommand command = new ConfirmarMfaCommand(1L, "482915");
        MensajeResponse mensajeResponse = new MensajeResponse(
            "El doble factor de autenticación quedó activado. El próximo inicio de sesión te pedirá el código de acceso.");
        when(authRestMapper.toCommand(confirmarRequest, 1L)).thenReturn(command);
        when(confirmarMfaUseCase.confirmarMfa(command)).thenReturn(mensajeResponse);
        when(authRestMapper.toRestResponse(mensajeResponse)).thenReturn(new MensajeRestResponse(mensajeResponse.mensaje()));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/mfa/confirmar", confirmarRequest, usuarioAutenticado(1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value(
                "El doble factor de autenticación quedó activado. El próximo inicio de sesión te pedirá el código de acceso."));

        verify(confirmarMfaUseCase, times(1)).confirmarMfa(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/confirmar - Debería retornar 401 Unauthorized cuando el código es incorrecto")
    void deberiaRetornarUnauthorized_CuandoElCodigoDeActivacionEsIncorrecto() throws Exception {
        // Arrange
        ConfirmarMfaRestRequest confirmarRequest = new ConfirmarMfaRestRequest("111111");
        ConfirmarMfaCommand command = new ConfirmarMfaCommand(1L, "111111");
        when(authRestMapper.toCommand(confirmarRequest, 1L)).thenReturn(command);
        when(confirmarMfaUseCase.confirmarMfa(command))
            .thenThrow(new OtpInvalidoException("El código de activación es incorrecto. Intentos restantes: 3."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/mfa/confirmar", confirmarRequest, usuarioAutenticado(1L))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("El código de activación es incorrecto. Intentos restantes: 3."));

        verify(confirmarMfaUseCase, times(1)).confirmarMfa(command);
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/mfa - Debería desactivar el doble factor y responder el mensaje de confirmación")
    void deberiaDesactivarElDobleFactor_CuandoLaContrasenaEsCorrecta() throws Exception {
        // Arrange
        DesactivarMfaRestRequest desactivarRequest = DesactivarMfaRestRequest.builder().password(PASSWORD).build();
        DesactivarMfaCommand command = new DesactivarMfaCommand(1L, PASSWORD);
        MensajeResponse mensajeResponse = new MensajeResponse(
            "El doble factor de autenticación quedó desactivado. Inicia sesión solo con tu email y contraseña.");
        when(authRestMapper.toCommand(desactivarRequest, 1L)).thenReturn(command);
        when(desactivarMfaUseCase.desactivarMfa(command)).thenReturn(mensajeResponse);
        when(authRestMapper.toRestResponse(mensajeResponse)).thenReturn(new MensajeRestResponse(mensajeResponse.mensaje()));

        // Act & Assert
        deleteJson(mockMvc, objectMapper, "/api/v1/auth/mfa", desactivarRequest, usuarioAutenticado(1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value(
                "El doble factor de autenticación quedó desactivado. Inicia sesión solo con tu email y contraseña."));

        verify(desactivarMfaUseCase, times(1)).desactivarMfa(command);
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/mfa - Debería retornar 401 Unauthorized cuando la contraseña es incorrecta")
    void deberiaRetornarUnauthorized_CuandoLaContrasenaEsIncorrectaAlDesactivar() throws Exception {
        // Arrange
        DesactivarMfaRestRequest desactivarRequest = DesactivarMfaRestRequest.builder().password("contrasena erronea").build();
        DesactivarMfaCommand command = new DesactivarMfaCommand(1L, "contrasena erronea");
        when(authRestMapper.toCommand(desactivarRequest, 1L)).thenReturn(command);
        when(desactivarMfaUseCase.desactivarMfa(command))
            .thenThrow(new CredencialInvalidoException("La contraseña del usuario es incorrecta."));

        // Act & Assert
        deleteJson(mockMvc, objectMapper, "/api/v1/auth/mfa", desactivarRequest, usuarioAutenticado(1L))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("La contraseña del usuario es incorrecta."));

        verify(desactivarMfaUseCase, times(1)).desactivarMfa(command);
    }

    // ==========================================
    // REGISTER
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/register - Debería registrar el usuario exitosamente cuando el body es válido")
    void deberiaRegistrarUsuarioExitosamente_CuandoElBodyEsValido() throws Exception {
        // Arrange
        RegisterCommand command = new RegisterCommand(EMAIL, PASSWORD, Set.of(2L));
        UsuarioResponse useCaseResponse = new UsuarioResponse(1L, EMAIL, true, false, Set.of(
            new RolResponse(2L, "PACIENTE", Set.of(new PermisoResponse(1L, "PERMISO_READ")))
        ));
        when(authRestMapper.toCommand(registerRequest)).thenReturn(command);
        when(registerUseCase.register(command)).thenReturn(useCaseResponse);

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/register", registerRequest)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.roles[0].nombre").value("PACIENTE"));

        verify(registerUseCase, times(1)).register(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestEnRegister_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        RegisterRestRequest requestInvalido = new RegisterRestRequest("", "", Set.of());

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/register", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, registerUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debería retornar 400 Bad Request cuando la contraseña no cumple la política de seguridad")
    void deberiaRetornarBadRequestEnRegister_CuandoLaContrasenaNoCumpleLaPolitica() throws Exception {
        // Arrange
        RegisterRestRequest requestContrasenaDebil = new RegisterRestRequest(EMAIL, "abc12", Set.of(2L));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/register", requestContrasenaDebil)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.password").value(ReglaContrasena.MENSAJE));

        verifyNoInteractions(authRestMapper, registerUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debería retornar 409 Conflict cuando el email ya está registrado")
    void deberiaRetornarConflict_CuandoElEmailYaEstaRegistrado() throws Exception {
        // Arrange
        RegisterCommand command = new RegisterCommand(EMAIL, PASSWORD, Set.of(2L));
        when(authRestMapper.toCommand(registerRequest)).thenReturn(command);
        when(registerUseCase.register(command))
            .thenThrow(new UsuarioDuplicadoException("Ya existe un usuario registrado con el email: " + EMAIL));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/register", registerRequest)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("Ya existe un usuario registrado con el email: " + EMAIL));

        verify(registerUseCase, times(1)).register(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debería retornar 404 Not Found cuando el rol a asignar no existe")
    void deberiaRetornarNotFound_CuandoElRolNoExiste() throws Exception {
        // Arrange
        RegisterCommand command = new RegisterCommand(EMAIL, PASSWORD, Set.of(2L));
        when(authRestMapper.toCommand(registerRequest)).thenReturn(command);
        when(registerUseCase.register(command))
            .thenThrow(new RecursoNoEncontradoException("Uno o más roles proporcionados no existen.", Set.of(2L)));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/register", registerRequest)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Uno o más roles proporcionados no existen."))
            .andExpect(jsonPath("$.notFoundIds[0]").value(2));

        verify(registerUseCase, times(1)).register(command);
    }

    // ==========================================
    // REFRESH
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debería renovar los tokens exitosamente cuando el refresh token es válido")
    void deberiaRenovarTokensExitosamente_CuandoElRefreshTokenEsValido() throws Exception {
        // Arrange
        RefreshTokenCommand command = new RefreshTokenCommand(REFRESH_TOKEN);
        when(authRestMapper.toCommand(refreshRequest)).thenReturn(command);
        when(refreshTokenUseCase.refreshToken(command)).thenReturn(authResponse);
        when(authRestMapper.toRestResponse(authResponse)).thenReturn(authRestResponse);

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/refresh", refreshRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
            .andExpect(jsonPath("$.refreshToken").value(NEW_REFRESH_TOKEN))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(refreshTokenUseCase, times(1)).refreshToken(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestEnRefresh_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        RefreshTokenRestRequest requestInvalido = new RefreshTokenRestRequest("");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/refresh", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, refreshTokenUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debería retornar 401 Unauthorized cuando el refresh token es inválido o expirado")
    void deberiaRetornarUnauthorized_CuandoElRefreshTokenEsInvalidoOExpirado() throws Exception {
        // Arrange
        RefreshTokenCommand command = new RefreshTokenCommand(REFRESH_TOKEN);
        when(authRestMapper.toCommand(refreshRequest)).thenReturn(command);
        when(refreshTokenUseCase.refreshToken(command))
            .thenThrow(new CredencialInvalidoException("Refresh token inválido o expirado."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/refresh", refreshRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Refresh token inválido o expirado."));

        verify(refreshTokenUseCase, times(1)).refreshToken(command);
    }

    // ==========================================
    // FORGOT PASSWORD
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - Debería responder 200 con el mensaje informativo del caso de uso")
    void deberiaResponder200ConMensaje_CuandoSeSolicitaRecuperacion() throws Exception {
        // Arrange
        ForgotPasswordCommand command = new ForgotPasswordCommand(EMAIL);
        when(authRestMapper.toCommand(forgotRequest)).thenReturn(command);
        when(forgotPasswordUseCase.forgotPassword(command)).thenReturn(new MensajeResponse(MENSAJE_GENERICO));
        when(authRestMapper.toRestResponse(new MensajeResponse(MENSAJE_GENERICO)))
            .thenReturn(new MensajeRestResponse(MENSAJE_GENERICO));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/forgot-password", forgotRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value(MENSAJE_GENERICO));

        verify(forgotPasswordUseCase, times(1)).forgotPassword(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestEnForgotPassword_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        ForgotPasswordRestRequest requestInvalido = new ForgotPasswordRestRequest("no-es-un-email");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/forgot-password", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, forgotPasswordUseCase);
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Debería responder 200 con el mensaje de éxito del caso de uso")
    void deberiaResponder200ConMensajeDeExito_CuandoElTokenEsValido() throws Exception {
        // Arrange
        ResetPasswordCommand command = new ResetPasswordCommand(RESET_TOKEN, PASSWORD);
        when(authRestMapper.toCommand(resetRequest)).thenReturn(command);
        when(resetPasswordUseCase.resetPassword(command)).thenReturn(new MensajeResponse(
            "La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales."));
        when(authRestMapper.toRestResponse(new MensajeResponse(
            "La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales.")))
            .thenReturn(new MensajeRestResponse(
                "La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/reset-password", resetRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje")
                .value("La contraseña fue restablecida correctamente. Inicia sesión con tus nuevas credenciales."));

        verify(resetPasswordUseCase, times(1)).resetPassword(command);
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestEnResetPassword_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        ResetPasswordRestRequest requestInvalido = new ResetPasswordRestRequest("", "");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/reset-password", requestInvalido)
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authRestMapper, resetPasswordUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Debería retornar 400 Bad Request cuando la nueva contraseña no cumple la política de seguridad")
    void deberiaRetornarBadRequestEnResetPassword_CuandoLaNuevaContrasenaNoCumpleLaPolitica() throws Exception {
        // Arrange
        ResetPasswordRestRequest requestContrasenaDebil = new ResetPasswordRestRequest(RESET_TOKEN, "abc12");

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/reset-password", requestContrasenaDebil)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.newPassword").value(ReglaContrasena.MENSAJE));

        verifyNoInteractions(authRestMapper, resetPasswordUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Debería retornar 401 Unauthorized cuando el token de recuperación es inválido o expirado")
    void deberiaRetornarUnauthorized_CuandoElTokenDeRecuperacionEsInvalidoOExpirado() throws Exception {
        // Arrange
        ResetPasswordCommand command = new ResetPasswordCommand(RESET_TOKEN, PASSWORD);
        when(authRestMapper.toCommand(resetRequest)).thenReturn(command);
        when(resetPasswordUseCase.resetPassword(command))
            .thenThrow(new CredencialInvalidoException("El token de recuperación es inválido o expirado."));

        // Act & Assert
        postJson(mockMvc, objectMapper, "/api/v1/auth/reset-password", resetRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("El token de recuperación es inválido o expirado."));

        verify(resetPasswordUseCase, times(1)).resetPassword(command);
    }
}
