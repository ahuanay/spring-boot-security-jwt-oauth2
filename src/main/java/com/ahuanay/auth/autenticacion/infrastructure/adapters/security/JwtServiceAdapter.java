package com.ahuanay.auth.autenticacion.infrastructure.adapters.security;

import com.ahuanay.auth.autenticacion.application.port.out.JwtServicePort;
import com.ahuanay.auth.autenticacion.application.response.RefreshTokenGenerado;
import com.ahuanay.auth.autenticacion.domain.exception.CredencialInvalidoException;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.rol.domain.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceAdapter implements JwtServicePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtServiceAdapter.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.timezone:America/Lima}")
    private ZoneId zonaAplicacion;

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put(JwtClaims.USUARIO_ID, usuario.getId());

        var roles = usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet());
        var permisos = usuario.getRoles().stream()
            .flatMap(r -> r.getPermisos().stream())
            .map(Permiso::getNombre)
            .collect(Collectors.toSet());

        extraClaims.put(JwtClaims.ROLES, roles);
        extraClaims.put(JwtClaims.PERMISOS, permisos);

        return buildToken(extraClaims, usuario.getEmail(), jwtExpiration);
    }

    @Override
    public RefreshTokenGenerado generateRefreshToken(Usuario usuario) {
        Date expiracion = new Date(System.currentTimeMillis() + refreshExpiration);
        String token = buildToken(new HashMap<>(), usuario.getEmail(), expiracion);
        return new RefreshTokenGenerado(
            token,
            LocalDateTime.ofInstant(expiracion.toInstant(), zonaAplicacion)
        );
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return buildToken(extraClaims, subject, new Date(System.currentTimeMillis() + expiration));
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, Date expiration) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(expiration)
            .signWith(getSignInKey(), Jwts.SIG.HS256)
            .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.warn("Failed to parse JWT token: {}", ex.getMessage());
            throw new CredencialInvalidoException("El token es inválido o ha expirado.");
        }
    }

    @Override
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}