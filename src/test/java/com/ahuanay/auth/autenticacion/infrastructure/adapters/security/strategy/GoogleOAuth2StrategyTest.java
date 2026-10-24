package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleOAuth2StrategyTest {

    private GoogleOAuth2Strategy googleOAuth2Strategy;

    @BeforeEach
    void setUp() {
        googleOAuth2Strategy = new GoogleOAuth2Strategy();
    }

    private OAuth2User crearOAuth2User(Map<String, Object> atributos, String nombreAtributo) {
        Set<GrantedAuthority> autoridades = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new DefaultOAuth2User(autoridades, atributos, nombreAtributo);
    }

    @Test
    @DisplayName("Debería retornar el proveedor GOOGLE cuando se invoca getProvider")
    void deberiaRetornarProveedorGoogle_CuandoSeInvocaGetProvider() {
        // Act
        AuthProvider provider = googleOAuth2Strategy.getProvider();

        // Assert
        assertThat(provider).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Debería extraer el email cuando el usuario de Google proporciona sus atributos")
    void deberiaExtraerEmail_CuandoElUsuarioDeGoogleProporcionaSusAtributos() {
        // Arrange
        Map<String, Object> atributos = Map.of(
            "email", "user@gmail.com",
            "sub", "1234567890"
        );
        OAuth2User oauthUser = crearOAuth2User(atributos, "sub");

        // Act
        String email = googleOAuth2Strategy.extractEmail(oauthUser);

        // Assert
        assertThat(email).isEqualTo("user@gmail.com");
    }

    @Test
    @DisplayName("Debería extraer el ID de proveedor cuando se consulta el atributo sub del usuario de Google")
    void deberiaExtraerProviderId_CuandoSeConsultaElAtributoSubDelUsuarioDeGoogle() {
        // Arrange
        Map<String, Object> atributos = Map.of(
            "email", "user@gmail.com",
            "sub", "1234567890"
        );
        OAuth2User oauthUser = crearOAuth2User(atributos, "sub");

        // Act
        String providerId = googleOAuth2Strategy.extractProviderId(oauthUser);

        // Assert
        assertThat(providerId).isEqualTo("1234567890");
    }
}