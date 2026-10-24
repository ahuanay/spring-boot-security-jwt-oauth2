package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.VerificarOtpCommand;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpBloqueadoException;
import com.ahuanay.auth.autenticacion.domain.exception.OtpInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.support.TestTokens;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarOtpUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String TOKEN_MFA = "mfa-token-789";
    private static final String TOKEN_HASH = "hash-token";
    private static final String CODIGO = "482915";
    private static final String CODIGO_HASH = "hash-codigo";
    private static final int MAX_ATTEMPTS = 5;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private JwtServicePort jwtServicePort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @InjectMocks
    private VerificarOtpUseCaseImpl verificarOtpUseCase;

    private VerificarOtpCommand command;
    private MfaOtp otp;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        command = new VerificarOtpCommand(TOKEN_MFA, CODIGO);
        usuario = TestUsuarios.localConMfa();
        otp = TestTokens.otp(TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.LOGIN,
            LocalDateTime.now().plusMinutes(5));

        ReflectionTestUtils.setField(verificarOtpUseCase, "maxAttempts", MAX_ATTEMPTS);
    }

    @Test
    @DisplayName("Debería entregar los tokens JWT cuando el código OTP es correcto")
    void deberiaEntregarTokensJwt_CuandoElCodigoOtpEsCorrecto() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn(CODIGO_HASH);
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtServicePort.generateAccessToken(usuario)).thenReturn("access-token-123");
        when(jwtServicePort.generateRefreshToken(usuario))
            .thenReturn(new RefreshTokenGenerado("refresh-token-456", LocalDateTime.now().plusDays(7)));

        // Act
        AuthResponse response = verificarOtpUseCase.verificarOtp(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(mfaOtpRepositoryPort, times(1)).save(otp);
        assertThat(otp.estaUsado()).isTrue();
        verify(refreshTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el token intermedio no existe")
    void deberiaLanzarCredencialInvalidoException_CuandoElTokenIntermedioNoExiste() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verifyNoInteractions(usuarioRepositoryPort, jwtServicePort, refreshTokenRepositoryPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el OTP ya fue utilizado")
    void deberiaLanzarCredencialInvalidoException_CuandoElOtpYaFueUtilizado() {
        // Arrange
        otp.marcarComoUsado(LocalDateTime.now());
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verifyNoInteractions(usuarioRepositoryPort, jwtServicePort, refreshTokenRepositoryPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException y eliminarlo cuando el OTP ya expiró")
    void deberiaLanzarCredencialInvalidoExceptionYEliminarlo_CuandoElOtpYaExpiro() {
        // Arrange
        MfaOtp otpExpirado = TestTokens.otp(
            TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.LOGIN, LocalDateTime.now().minusMinutes(1));
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otpExpirado));

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El token de verificación es inválido, el código expiró o ya fue utilizado.");

        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verifyNoInteractions(usuarioRepositoryPort, jwtServicePort, refreshTokenRepositoryPort);
    }

    @Test
    @DisplayName("Debería registrar un intento fallido y lanzar OtpInvalidoException cuando el código es incorrecto")
    void deberiaRegistrarIntentoFallidoYLanzarOtpInvalidoException_CuandoElCodigoEsIncorrecto() {
        // Arrange
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn("otro-hash");

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(OtpInvalidoException.class)
            .hasMessage("El código es incorrecto. Intentos restantes: 4.");

        verify(mfaOtpRepositoryPort, times(1)).save(otp);
        assertThat(otp.getIntentos()).isEqualTo(1);
        verifyNoInteractions(usuarioRepositoryPort, jwtServicePort, refreshTokenRepositoryPort);
    }

    @Test
    @DisplayName("Debería eliminar el desafío y lanzar OtpBloqueadoException cuando se agotan los intentos disponibles")
    void deberiaEliminarElDesafioYLanzarOtpBloqueadoException_CuandoSeAgotanLosIntentosDisponibles() {
        // Arrange
        MfaOtp otpUltimoIntento = TestTokens.otpCompleto(
            TOKEN_HASH, EMAIL, CODIGO_HASH, PropositoOtp.LOGIN,
            LocalDateTime.now().plusMinutes(5), MAX_ATTEMPTS - 1, null);
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otpUltimoIntento));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn("otro-hash");

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(OtpBloqueadoException.class)
            .hasMessage("Se superó el número máximo de intentos. Inicia sesión nuevamente para recibir un nuevo código.");

        verify(mfaOtpRepositoryPort, times(1)).deleteByTokenHash(TOKEN_HASH);
        verify(mfaOtpRepositoryPort, never()).save(any(MfaOtp.class));
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el usuario del OTP se encuentra inactivo")
    void deberiaLanzarCredencialInvalidoException_CuandoElUsuarioDelOtpSeEncuentraInactivo() {
        // Arrange
        Usuario usuarioInactivo = TestUsuarios.base().inactivo().conMfa().build();
        when(resetTokenGeneratorPort.hash(TOKEN_MFA)).thenReturn(TOKEN_HASH);
        when(mfaOtpRepositoryPort.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(otp));
        when(resetTokenGeneratorPort.hash(CODIGO)).thenReturn(CODIGO_HASH);
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        assertThatThrownBy(() -> verificarOtpUseCase.verificarOtp(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El usuario se encuentra inactivo.");

        verifyNoInteractions(jwtServicePort, refreshTokenRepositoryPort);
    }
}