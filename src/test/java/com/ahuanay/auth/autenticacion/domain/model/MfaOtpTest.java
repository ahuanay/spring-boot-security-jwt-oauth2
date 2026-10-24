package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfaOtpTest {

    private static final LocalDateTime EXPIRACION = LocalDateTime.now().plusMinutes(5);

    @Test
    @DisplayName("Debería crear un OTP con propósito LOGIN por defecto cuando el propósito llega nulo")
    void deberiaCrearOtpConPropositoLoginPorDefecto_CuandoPropositoLlegaNulo() {
        // Arrange & Act
        MfaOtp otp = new MfaOtp(
            null, "hash-token", "user@email.com", "hash-codigo", null, EXPIRACION, 0, null);

        // Assert
        assertThat(otp.getProposito()).isEqualTo(PropositoOtp.LOGIN);
        assertThat(otp.getIntentos()).isZero();
        assertThat(otp.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el hash del token está vacío")
    void deberiaLanzarCredencialInvalidoException_CuandoElHashDelTokenEstaVacio() {
        // Act & Assert
        assertThatThrownBy(() -> new MfaOtp(
            null, " ", "user@email.com", "hash-codigo", PropositoOtp.LOGIN, EXPIRACION, 0, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El hash del token de doble factor no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el hash del código está vacío")
    void deberiaLanzarCredencialInvalidoException_CuandoElHashDelCodigoEstaVacio() {
        // Act & Assert
        assertThatThrownBy(() -> new MfaOtp(
            null, "hash-token", "user@email.com", "", PropositoOtp.ACTIVACION, EXPIRACION, 0, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El hash del código OTP no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar CredencialInvalidoException cuando el email está vacío")
    void deberiaLanzarCredencialInvalidoException_CuandoElEmailEstaVacio() {
        // Act & Assert
        assertThatThrownBy(() -> new MfaOtp(
            null, "hash-token", "  ", "hash-codigo", PropositoOtp.LOGIN, EXPIRACION, 0, null))
            .isInstanceOf(CredencialInvalidoException.class)
            .hasMessage("El email asociado al código OTP no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería identificar un OTP como expirado cuando la fecha de expiración ya pasó")
    void deberiaIdentificarOtpComoExpirado_CuandoLaFechaDeExpiracionYaPaso() {
        // Arrange
        MfaOtp otp = new MfaOtp(
            null, "hash-token", "user@email.com", "hash-codigo", PropositoOtp.LOGIN,
            LocalDateTime.now().minusMinutes(1), 0, null);

        // Act & Assert
        assertThat(otp.estaExpirado(LocalDateTime.now())).isTrue();
        assertThat(otp.estaUsado()).isFalse();
    }

    @Test
    @DisplayName("Debería registrar los intentos fallidos y marcar como usado solo una vez cuando se invoca la mutación de estado")
    void deberiaRegistrarIntentosFallidosYMarcarComoUsadoSoloUnaVez_CuandoSeInvocaLaMutacionDeEstado() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();
        MfaOtp otp = new MfaOtp(
            null, "hash-token", "user@email.com", "hash-codigo", PropositoOtp.LOGIN, EXPIRACION, 0, null);

        // Act
        otp.registrarIntentoFallido();
        otp.registrarIntentoFallido();
        otp.marcarComoUsado(ahora);
        LocalDateTime primerUso = otp.getUsedAt();
        otp.marcarComoUsado(ahora.plusMinutes(1));

        // Assert
        assertThat(otp.getIntentos()).isEqualTo(2);
        assertThat(otp.estaUsado()).isTrue();
        assertThat(otp.getUsedAt()).isEqualTo(primerUso);
    }
}