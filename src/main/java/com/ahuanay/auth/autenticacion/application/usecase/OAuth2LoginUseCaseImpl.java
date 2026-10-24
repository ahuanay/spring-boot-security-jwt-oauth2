package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.port.in.OAuth2LoginUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.JwtServicePort;
import com.ahuanay.auth.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.exception.UsuarioInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.rol.application.port.out.RolRepositoryPort;
import com.ahuanay.auth.rol.domain.model.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuth2LoginUseCaseImpl implements OAuth2LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RolRepositoryPort rolRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final JwtServicePort jwtServicePort;

    @Value("${app.security.default-role}")
    private String defaultRoleName;

    @Override
    @Transactional
    public AuthResponse processOAuth2Login(String email, String providerId, AuthProvider provider) {
        Usuario usuario = usuarioRepositoryPort.findByEmail(email).orElseGet(() -> {
            Rol defaultRol = rolRepositoryPort.findByNombre(defaultRoleName)
                .orElseThrow(() -> new UsuarioInvalidoException("Rol por defecto " + defaultRoleName + " no encontrado"));

            Set<Rol> roles = new HashSet<>(Collections.singletonList(defaultRol));

            Usuario nuevoUsuario = new Usuario(
                null,
                email,
                null,
                provider,
                providerId,
                true,
                false,
                roles
            );
            return usuarioRepositoryPort.save(nuevoUsuario);
        });

        return authenticateAndGenerateTokens(usuario);
    }

    private AuthResponse authenticateAndGenerateTokens(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActive())) {
            throw new UsuarioInvalidoException("El usuario se encuentra inactivo.");
        }

        String accessToken = jwtServicePort.generateAccessToken(usuario);
        RefreshTokenGenerado refreshTokenGenerado = jwtServicePort.generateRefreshToken(usuario);

        refreshTokenRepositoryPort.deleteByEmail(usuario.getEmail());
        refreshTokenRepositoryPort.save(new RefreshToken(
            null, refreshTokenGenerado.token(), usuario.getEmail(), refreshTokenGenerado.expiresAt()));

        return new AuthResponse(accessToken, refreshTokenGenerado.token());
    }
}