package com.proteinpro.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {
    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void validatesSignedIdentity() {
        String token = Jwts.builder()
                .subject("learner@example.com")
                .claim("userId", "user-1")
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        JwtTokenService.AuthenticatedUser user = new JwtTokenService(SECRET).validate(token);

        assertThat(user.userId()).isEqualTo("user-1");
        assertThat(user.email()).isEqualTo("learner@example.com");
    }

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder()
                .subject("learner@example.com")
                .claim("userId", "user-1")
                .expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> new JwtTokenService(SECRET).validate(token))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsShortSecretAndMissingIdentityClaims() {
        assertThatThrownBy(() -> new JwtTokenService("too-short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 characters");

        String missingUser = token("learner@example.com", null);
        String blankUser = token("learner@example.com", " ");
        String missingSubject = token(null, "user-1");

        JwtTokenService service = new JwtTokenService(SECRET);
        assertThatThrownBy(() -> service.validate(missingUser)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(blankUser)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(missingSubject)).isInstanceOf(IllegalArgumentException.class);
    }

    private String token(String subject, String userId) {
        var builder = Jwts.builder().expiration(Date.from(Instant.now().plusSeconds(60)));
        if (subject != null) builder.subject(subject);
        if (userId != null) builder.claim("userId", userId);
        return builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }
}
