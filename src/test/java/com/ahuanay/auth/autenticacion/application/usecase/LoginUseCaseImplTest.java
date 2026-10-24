package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.LoginCommand;
import com.ahuanay.auth.autenticacion.application.port.out.*;
import com.ahuanay.auth.autenticacion.application.response.LoginResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
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
class LoginUseCaseImplTest {

    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "SecurePassword123*";
    private static final String HASH = "$2a$10$hashSimulado";

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private MfaOtpRepositoryPort mfaOtpRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private JwtServicePort jwtServicePort;

    @Mock
    private ResetTokenGeneratorPort resetTokenGeneratorPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private LoginCommand command;
    private Usuario usuario;
    private Usuario usuarioConMfa;
    private RefreshTokenGenerado refreshTokenGenerado;

    @BeforeEach
    void setUp() {
        command = new LoginCommand(EMAIL, PASSWORD);
        usuario = TestUsuarios.local();
        usuarioConMfa = TestUsuarios.localConMfa();
        refreshTokenGenerado = new RefreshTokenGenerado("refresh-token-456", LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(loginUseCase, "otpExpirationMinutes", 5);
    }

    @Test
    @DisplayName("Debería autenticar y retornar tokens JWT cuando las credenciales son válidas y el 2FA está desactivado")
    void deberiaAutenticarYRetornarTokens_CuandoCredencialesSonValidasYEl2FAEstaDesactivado() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(true);
        when(jwtServicePort.generateAccessToken(usuario)).thenReturn("access-token-123");
        when(jwtServicePort.generateRefreshToken(usuario)).thenReturn(refreshTokenGenerado);

        // Act
        LoginResponse response = loginUseCase.login(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mfaRequerido()).isFalse();
        assertThat(response.credenciales()).isNotNull();
        assertThat(response.credenciales().accessToken()).isEqualTo("access-token-123");
        assertThat(response.credenciales().refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.credenciales().tokenType()).isEqualTo("Bearer");
        assertThat(response.tokenMfa()).isNull();
        assertThat(response.expiraEnSegundos()).isNull();

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(passwordHasherPort, times(1)).matches(PASSWORD, HASH);
        verify(jwtServicePort, times(1)).generateAccessToken(usuario);
        verify(jwtServicePort, times(1)).generateRefreshToken(usuario);
        verify(refreshTokenRepositoryPort, times(1)).deleteByEmail(EMAIL);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
        verifyNoInteractions(mfaOtpRepositoryPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el usuario no existe")
    void deberiaLanzarCredencialInvalidoException_CuandoUsuarioNoExiste() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Credenciales inválidas.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verifyNoInteractions(passwordHasherPort, jwtServicePort, refreshTokenRepositoryPort, mfaOtpRepositoryPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el usuario se encuentra inactivo")
    void deberiaLanzarCredencialInvalidoException_CuandoUsuarioSeEncuentraInactivo() {
        // Arrange
        Usuario usuarioInactivo = TestUsuarios.localInactivo();
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El usuario se encuentra inactivo.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verifyNoInteractions(passwordHasherPort, jwtServicePort, refreshTokenRepositoryPort, mfaOtpRepositoryPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando la contraseña no coincide")
    void deberiaLanzarCredencialInvalidoException_CuandoLaContrasenaNoCoincide() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(command))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("Credenciales inválidas.");

        verify(usuarioRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(passwordHasherPort, times(1)).matches(PASSWORD, HASH);
        verifyNoInteractions(jwtServicePort, refreshTokenRepositoryPort, mfaOtpRepositoryPort, emailSenderPort);
    }

    @Test
    @DisplayName("Debería iniciar el desafío de doble factor sin emitir tokens cuando el usuario tiene 2FA activado")
    void deberiaIniciarDesafioOtpSinEmitirTokens_CuandoElUsuarioTiene2FAActivado() {
        // Arrange
        when(usuarioRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioConMfa));
        when(passwordHasherPort.matches(PASSWORD, HASH)).thenReturn(true);
        when(resetTokenGeneratorPort.generarToken()).thenReturn("mfa-token-789");
        when(resetTokenGeneratorPort.hash("mfa-token-789")).thenReturn("hash-token");
        when(resetTokenGeneratorPort.generarCodigo(MfaOtp.LONGITUD_CODIGO)).thenReturn("482915");
        when(resetTokenGeneratorPort.hash("482915")).thenReturn("hash-codigo");

        // Act
        LoginResponse response = loginUseCase.login(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mfaRequerido()).isTrue();
        assertThat(response.credenciales()).isNull();
        assertThat(response.tokenMfa()).isEqualTo("mfa-token-789");
        assertThat(response.expiraEnSegundos()).isEqualTo(300L);

        verify(mfaOtpRepositoryPort, times(1)).deleteByEmailAndProposito(EMAIL, PropositoOtp.LOGIN);
        verify(mfaOtpRepositoryPort, times(1)).save(any(MfaOtp.class));
        verify(emailSenderPort, times(1)).enviarOtpAcceso(EMAIL, "482915", 5);
        verifyNoInteractions(jwtServicePort, refreshTokenRepositoryPort);
    }
}
