package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.constraint;

import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContrasenaSeguraValidatorTest {

    private static final String LIMITE_SUPERIOR = "Aa1!" + "x".repeat(68);

    private static final String MUY_LARGA = "Aa1!" + "x".repeat(69);

    private static ValidatorFactory factory;
    private static Validator validator;

    private static class Formulario {
        @ContrasenaSegura
        String password;

        Formulario(String password) {
            this.password = password;
        }
    }

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SecurePassword123*",     // mayúscula + minúscula + número + símbolo *
        "NewSecurePassword123#",  // símbolo #
        "OtraClave123@-",         // símbolo - al final
        "Abcdefgh1.",             // símbolo .
        "Aa1@$!%*?&#+._X",        // todos los símbolos permitidos a la vez
        "OtraClave-1"             // solo el guion como símbolo
    })
    @DisplayName("Debería aceptar las contraseñas que cumplen la política")
    void deberiaAceptarContrasenas_CuandoCumplenLaPolitica(String contrasena) {
        // Act & Assert
        assertThat(validator.validate(new Formulario(contrasena))).isEmpty();
    }

    @Test
    @DisplayName("Debería aceptar una contraseña de exactamente 72 caracteres")
    void deberiaAceptarContrasena_CuandoTieneElLimiteSuperiorDeCaracteres() {
        // Act & Assert
        assertThat(validator.validate(new Formulario(LIMITE_SUPERIOR))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "abc12",      // demasiado corta (caso típico de contraseña débil)
        "Ab1!",       // demasiado corta cumpliendo los grupos
        "ABCDEFG1",   // sin símbolo: las mayúsculas no deben contar como símbolo
        "abcdefg1!",  // sin mayúscula
        "ABCDEFG1!",  // sin minúscula
        "Abcdefgh!",  // sin número
        "AbcdefgH1"   // sin símbolo
    })
    @DisplayName("Debería rechazar las contraseñas débiles y reportar el mensaje único de la política")
    void deberiaRechazarContrasenas_CuandoNoCumplenLaPolitica(String contrasena) {
        // Act
        var violaciones = validator.validate(new Formulario(contrasena));

        // Assert
        assertThat(violaciones).hasSize(1);
        assertThat(violaciones.iterator().next().getMessage()).isEqualTo(ReglaContrasena.MENSAJE);
    }

    @Test
    @DisplayName("Debería rechazar una contraseña con más de 72 caracteres")
    void deberiaRechazarContrasena_CuandoSuperaElLimiteDeCaracteres() {
        // Act
        var violaciones = validator.validate(new Formulario(MUY_LARGA));

        // Assert
        assertThat(violaciones).hasSize(1);
        assertThat(violaciones.iterator().next().getMessage()).isEqualTo(ReglaContrasena.MENSAJE);
    }

    @Test
    @DisplayName("Debería aceptar el valor nulo para que la obligatoriedad la trate @NotBlank")
    void deberiaAceptarValorNulo_ParaDelegarLaObligatoriedadEnNotBlank() {
        // Act & Assert
        assertThat(validator.validate(new Formulario(null))).isEmpty();
    }

    @Test
    @DisplayName("Debería usar el mensaje de la política por defecto en la anotación")
    void deberiaUsarElMensajeDeLaPolitica_ComoMensajePorDefecto() throws NoSuchFieldException {
        // Act
        var anotacion = Formulario.class.getDeclaredField("password")
            .getAnnotation(ContrasenaSegura.class);

        // Assert
        assertThat(anotacion.message()).isEqualTo(ReglaContrasena.MENSAJE);
    }
}
