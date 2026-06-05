package com.example.api_skincare.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida tokens JWT usando el secreto definido en application.properties
 * (jwt.secret). En producción se sobreescribe con la variable de entorno JWT_SECRET.
 */
@Component
public class JwtUtil {

    private static final long EXPIRATION_MS = 15 * 60 * 1000L; // 15 minutos

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Genera un token con el email del usuario como subject y su rol como claim. */
    public String generate(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key())
                .compact();
    }

    /** Lanza excepción si el token es inválido o expiró. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmail(String token) {
        return parse(token).getSubject();
    }

    public String getRol(String token) {
        return parse(token).get("rol", String.class);
    }
}
