package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception.SecurityExceptionDelegator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityExceptionDelegator securityExceptionDelegator;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.public-paths}")
    private List<String> rutasPublicas;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(rutasPublicas.toArray(String[]::new)).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionDelegator)
                .accessDeniedHandler(securityExceptionDelegator)
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(originalKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();
        rolesConverter.setAuthoritiesClaimName(JwtClaims.ROLES);
        rolesConverter.setAuthorityPrefix(JwtClaims.PREFIJO_ROL);

        JwtGrantedAuthoritiesConverter permisosConverter = new JwtGrantedAuthoritiesConverter();
        permisosConverter.setAuthoritiesClaimName(JwtClaims.PERMISOS);
        permisosConverter.setAuthorityPrefix("");

        return createConverter(rolesConverter, permisosConverter);
    }

    private static @NonNull JwtAuthenticationConverter createConverter(JwtGrantedAuthoritiesConverter rolesConverter, JwtGrantedAuthoritiesConverter permisosConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Collection<GrantedAuthority> roles = rolesConverter.convert(jwt);
            if (!roles.isEmpty()) {
                authorities.addAll(roles);
            }

            Collection<GrantedAuthority> permisos = permisosConverter.convert(jwt);
            if (!permisos.isEmpty()) {
                authorities.addAll(permisos);
            }

            return authorities;
        });
        return converter;
    }
}