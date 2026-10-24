package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuth2StrategyRegistryTest {

    private OAuth2StrategyRegistry oAuth2StrategyRegistry;

    @BeforeEach
    void setUp() {
        // Act
        oAuth2StrategyRegistry = new OAuth2StrategyRegistry(List.of(
            new GoogleOAuth2Strategy(),
            new GithubOAuth2Strategy()
        ));
    }

    @Test
    @DisplayName("Debería retornar la estrategia de Google cuando el proveedor consultado es GOOGLE")
    void deberiaRetornarEstrategiaGoogle_CuandoElProveedorEsGoogle() {
        // Act
        OAuth2UserInfoStrategy estrategia = oAuth2StrategyRegistry.getStrategy(AuthProvider.GOOGLE);

        // Assert
        assertThat(estrategia).isInstanceOf(GoogleOAuth2Strategy.class);
        assertThat(estrategia.getProvider()).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Debería retornar la estrategia de GitHub cuando el proveedor consultado es GITHUB")
    void deberiaRetornarEstrategiaGithub_CuandoElProveedorEsGithub() {
        // Act
        OAuth2UserInfoStrategy estrategia = oAuth2StrategyRegistry.getStrategy(AuthProvider.GITHUB);

        // Assert
        assertThat(estrategia).isInstanceOf(GithubOAuth2Strategy.class);
        assertThat(estrategia.getProvider()).isEqualTo(AuthProvider.GITHUB);
    }

    @Test
    @DisplayName("Debería lanzar UsuarioInvalidoException cuando no existe estrategia registrada para el proveedor")
    void deberiaLanzarUsuarioInvalidoException_CuandoNoExisteEstrategiaParaElProveedor() {
        // Act & Assert
        assertThatThrownBy(() -> oAuth2StrategyRegistry.getStrategy(AuthProvider.LOCAL))
            .isInstanceOf(UsuarioInvalidoException.class)
            .hasMessage("No existe una estrategia de OAuth2 para el proveedor: LOCAL");
    }
}