package com.ahuanay.auth.autenticacion.infrastructure.adapters.security.strategy;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GithubOAuth2StrategyTest {

    private GithubOAuth2Strategy githubOAuth2Strategy;

    @BeforeEach
    void setUp() {
        githubOAuth2Strategy = new GithubOAuth2Strategy();
    }

    private OAuth2User crearOAuth2User(Map<String, Object> atributos, String nombreAtributo) {
        Set<GrantedAuthority> autoridades = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new DefaultOAuth2User(autoridades, atributos, nombreAtributo);
    }

    @Test
    @DisplayName("Debería retornar el proveedor GITHUB cuando se invoca getProvider")
    void deberiaRetornarProveedorGithub_CuandoSeInvocaGetProvider() {
        // Act
        AuthProvider provider = githubOAuth2Strategy.getProvider();

        // Assert
        assertThat(provider).isEqualTo(AuthProvider.GITHUB);
    }

    @Test
    @DisplayName("Debería extraer el email cuando el usuario de GitHub lo comparte")
    void deberiaExtraerEmail_CuandoElUsuarioDeGithubLoComparte() {
        // Arrange
        Map<String, Object> atributos = Map.of(
            "email", "user@users.noreply.github.com",
            "login", "octocat"
        );
        OAuth2User oauthUser = crearOAuth2User(atributos, "login");

        // Act
        String email = githubOAuth2Strategy.extractEmail(oauthUser);

        // Assert
        assertThat(email).isEqualTo("user@users.noreply.github.com");
    }

    @Test
    @DisplayName("Debería derivar el email desde el login cuando el usuario de GitHub mantiene su email privado")
    void deberiaDerivarEmailDesdeElLogin_CuandoElUsuarioDeGithubMantieneSuEmailPrivado() {
        // Arrange
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("email", null);
        atributos.put("login", "octocat");
        OAuth2User oauthUser = crearOAuth2User(atributos, "login");

        // Act
        String email = githubOAuth2Strategy.extractEmail(oauthUser);

        // Assert
        assertThat(email).isEqualTo("octocat@github.local");
    }

    @Test
    @DisplayName("Debería derivar el email desde el login cuando el atributo email no viene en los atributos")
    void deberiaDerivarEmailDesdeElLogin_CuandoElAtributoEmailNoViene() {
        // Arrange
        Map<String, Object> atributos = Map.of("login", "octocat");
        OAuth2User oauthUser = crearOAuth2User(atributos, "login");

        // Act
        String email = githubOAuth2Strategy.extractEmail(oauthUser);

        // Assert
        assertThat(email).isEqualTo("octocat@github.local");
    }

    @Test
    @DisplayName("Debería extraer el ID de proveedor desde el login cuando se consulta el provider ID del usuario de GitHub")
    void deberiaExtraerProviderId_CuandoSeConsultaElLoginDelUsuarioDeGithub() {
        // Arrange
        Map<String, Object> atributos = Map.of("login", "octocat");
        OAuth2User oauthUser = crearOAuth2User(atributos, "login");

        // Act
        String providerId = githubOAuth2Strategy.extractProviderId(oauthUser);

        // Assert
        assertThat(providerId).isEqualTo("octocat");
    }
}