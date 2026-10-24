package com.ahuanay.auth.autenticacion.application.usecase;

import com.ahuanay.auth.autenticacion.application.command.RefreshTokenCommand;
import com.ahuanay.auth.autenticacion.application.port.in.RefreshTokenUseCase;
import com.ahuanay.auth.autenticacion.application.port.out.JwtServicePort;
import com.ahuanay.auth.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.ahuanay.auth.autenticacion.application.port.out.UsuarioRepositoryPort;
import com.ahuanay.auth.autenticacion.application.response.AuthResponse;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.RefreshToken;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final JwtServicePort jwtServicePort;

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenCommand command) {
        RefreshToken storedToken = refreshTokenRepositoryPort.findByToken(command.refreshToken())
            .orElseThrow(() -> new CredencialInvalidoException("Refresh token inválido o expirado."));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepositoryPort.deleteByToken(storedToken.getToken());
            throw new CredencialInvalidoException("Refresh token inválido o expirado.");
        }

        String email = jwtServicePort.extractUsername(storedToken.getToken());
        if (email == null || !jwtServicePort.isTokenValid(storedToken.getToken(), email)) {
            refreshTokenRepositoryPort.deleteByToken(storedToken.getToken());
            throw new CredencialInvalidoException("Refresh token inválido o expirado.");
        }

        Usuario usuario = usuarioRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new CredencialInvalidoException("Usuario no encontrado."));

        String newAccessToken = jwtServicePort.generateAccessToken(usuario);
        RefreshTokenGenerado refreshTokenGenerado = jwtServicePort.generateRefreshToken(usuario);

        refreshTokenRepositoryPort.deleteByToken(storedToken.getToken());
        refreshTokenRepositoryPort.save(new RefreshToken(
            null, refreshTokenGenerado.token(), usuario.getEmail(), refreshTokenGenerado.expiresAt()));

        return new AuthResponse(newAccessToken, refreshTokenGenerado.token());
    }
}
