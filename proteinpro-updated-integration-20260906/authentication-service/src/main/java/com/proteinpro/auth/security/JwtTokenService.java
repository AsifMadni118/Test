package com.proteinpro.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {
    private final SecretKey key;
    private final long expirationSeconds;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generate(String userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    public AuthenticatedUser validate(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        String userId = claims.get("userId", String.class);
        if (userId == null || userId.isBlank() || claims.getSubject() == null) {
            throw new IllegalArgumentException("Token is missing required identity claims");
        }
        return new AuthenticatedUser(userId, claims.getSubject());
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public record AuthenticatedUser(String userId, String email) {
    }
}
