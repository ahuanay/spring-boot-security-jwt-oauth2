package com.ahuanay.auth.autenticacion.domain.model;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.domain.model.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UsuarioTest {

    private static final Rol ROL = new Rol(2L, "PACIENTE", Set.of(new Permiso(1L, "PERMISO_READ")));
    private static final String POLITICA_CONTRASENA = ReglaContrasena.MENSAJE;

    @Test
    @DisplayName("Debería aplicar los valores por defecto: provider LOCAL y active true cuando son nulos")
    void deberiaAplicarValoresPorDefecto_CuandoProviderYActiveSonNulos() {
        // Act
        Usuario usuario = new Usuario(1L, "user@email.com", "password-hasheada", null, null, null, null, Set.of(ROL));

        // Assert
        assertThat(usuario.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(usuario.getActive()).isTrue();
        assertThat(usuario.getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando el email está vacío")
    void deberiaLanzarExcepcion_CuandoElEmailEstaVacio() {
        // Act & Assert
        assertThatThrownBy(() -> Usuario.validarReglasCreacion(" ", "SecurePassword123*", AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El email del usuario no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando el usuario LOCAL no tiene contraseña")
    void deberiaLanzarExcepcion_CuandoElUsuarioLocalNoTieneContrasena() {
        // Act & Assert (Contraseña nula)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", null, AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("La contraseña del usuario no puede ser nula o vacía.");

        // Act & Assert (Contraseña en blanco)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "  ", AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("La contraseña del usuario no puede ser nula o vacía.");
    }

    @Test
    @DisplayName("Debería permitir construir un usuario OAuth2 sin contraseña")
    void deberiaPermitirConstruirUsuarioOAuth2_CuandoNoTieneContrasena() {
        // Act
        Usuario usuario = new Usuario(1L, "user@email.com", null, AuthProvider.GOOGLE, "google-id", true, false, Set.of(ROL));

        // Assert
        assertThat(usuario.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(usuario.getPassword()).isNull();
        assertThat(usuario.getProviderId()).isEqualTo("google-id");
    }

    @Test
    @DisplayName("Debería aceptar las reglas de creación cuando todos los datos son válidos")
    void deberiaAceptarReglasCreacion_CuandoLosDatosSonValidos() {
        // Act & Assert
        assertThatCode(() -> Usuario.validarReglasCreacion("user@email.com", "SecurePassword123*", AuthProvider.LOCAL, Set.of(2L)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando la contraseña no cumple la política de seguridad")
    void deberiaLanzarExcepcion_CuandoLaContrasenaNoCumpleLaPoliticaDeSeguridad() {
        // Act & Assert (demasiado corta)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "abc12", AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage(POLITICA_CONTRASENA);

        // Act & Assert (sin símbolo especial)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "AbcdefgH1", AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage(POLITICA_CONTRASENA);

        // Act & Assert (más de 72 caracteres)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "Aa1!" + "x".repeat(69), AuthProvider.LOCAL, Set.of(2L)))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage(POLITICA_CONTRASENA);
    }

    @Test
    @DisplayName("Debería aceptar una contraseña débil para un usuario OAuth2 porque no se usa como credencial local")
    void deberiaAceptarContrasenaDebil_CuandoElUsuarioEsOAuth2() {
        // Act & Assert
        assertThatCode(() -> Usuario.validarReglasCreacion("user@email.com", "abc12", AuthProvider.GOOGLE, Set.of(2L)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando no se asigna ningún rol en la creación")
    void deberiaLanzarExcepcion_CuandoNoSeAsignaNingunRolEnLaCreacion() {
        // Act & Assert (Set vacío)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "SecurePassword123*", AuthProvider.LOCAL, Set.of()))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario debe tener al menos un rol asignado.");

        // Act & Assert (Set nulo)
        assertThatThrownBy(() -> Usuario.validarReglasCreacion("user@email.com", "SecurePassword123*", AuthProvider.LOCAL, null))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario debe tener al menos un rol asignado.");
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException al instanciar el objeto directamente sin roles")
    void deberiaLanzarExcepcionAlInstanciar_CuandoRolesEstanVaciosONulos() {
        // Act & Assert (Roles vacíos en constructor)
        assertThatThrownBy(() -> new Usuario(1L, "user@email.com", "pass", AuthProvider.LOCAL, null, true, false, Set.of()))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario debe tener al menos un rol asignado.");

        // Act & Assert (Roles nulos en constructor)
        assertThatThrownBy(() -> new Usuario(1L, "user@email.com", "pass", AuthProvider.LOCAL, null, true, false, null))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("El usuario debe tener al menos un rol asignado.");
    }
}