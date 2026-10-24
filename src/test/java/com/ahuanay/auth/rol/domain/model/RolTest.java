package com.ahuanay.auth.rol.domain.model;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.domain.exception.RolInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class RolTest {

    private static final Permiso PERMISO = new Permiso(1L, "PERMISO_READ");

    @Test
    @DisplayName("Debería construir el rol con sus datos y una copia inmutable de los permisos")
    void deberiaConstruirRol_ConCopiaInmutableDePermisos() {
        // Act
        Rol rol = new Rol(10L, "DOCTOR", Set.of(PERMISO));

        // Assert
        assertThat(rol.getId()).isEqualTo(10L);
        assertThat(rol.getNombre()).isEqualTo("DOCTOR");
        assertThat(rol.getPermisos()).containsExactly(PERMISO);
        assertThat(rol.getPermisos()).isUnmodifiable();
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando el nombre es nulo")
    void deberiaLanzarRolInvalidoException_CuandoElNombreEsNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new Rol(null, null, Set.of(PERMISO)))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El nombre del rol no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando el nombre está en blanco")
    void deberiaLanzarRolInvalidoException_CuandoElNombreEstaEnBlanco() {
        // Act & Assert
        assertThatThrownBy(() -> new Rol(null, "   ", Set.of(PERMISO)))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El nombre del rol no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando los permisos son nulos")
    void deberiaLanzarRolInvalidoException_CuandoLosPermisosSonNulos() {
        // Act & Assert
        assertThatThrownBy(() -> new Rol(null, "DOCTOR", null))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El rol debe tener al menos un permiso asignado.");
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException cuando el rol no tiene permisos")
    void deberiaLanzarRolInvalidoException_CuandoElRolNoTienePermisos() {
        // Act & Assert
        assertThatThrownBy(() -> new Rol(null, "DOCTOR", Set.of()))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El rol debe tener al menos un permiso asignado.");
    }

    @Test
    @DisplayName("Debería aceptar las reglas de creación cuando el nombre y los permisos son válidos")
    void deberiaAceptarReglasCreacion_CuandoNombreYPermisosSonValidos() {
        // Act & Assert
        assertThatCode(() -> Rol.validarReglasCreacion("DOCTOR", Set.of(1L, 2L)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException en las reglas de creación cuando el nombre no es válido")
    void deberiaLanzarRolInvalidoExceptionEnReglasCreacion_CuandoElNombreNoEsValido() {
        // Act & Assert
        assertThatThrownBy(() -> Rol.validarReglasCreacion("", Set.of(1L)))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El nombre del rol no puede estar vacío.");
    }

    @Test
    @DisplayName("Debería lanzar RolInvalidoException en las reglas de creación cuando no hay permisos")
    void deberiaLanzarRolInvalidoExceptionEnReglasCreacion_CuandoNoHayPermisos() {
        // Act & Assert
        assertThatThrownBy(() -> Rol.validarReglasCreacion("DOCTOR", null))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El rol debe tener al menos un permiso asignado.");

        assertThatThrownBy(() -> Rol.validarReglasCreacion("DOCTOR", Set.of()))
            .isInstanceOf(RolInvalidoException.class)
            .hasMessage("El rol debe tener al menos un permiso asignado.");
    }
}