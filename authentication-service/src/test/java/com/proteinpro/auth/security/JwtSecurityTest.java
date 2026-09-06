package com.proteinpro.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecurityTest {
    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generatesAndValidatesIdentityToken() {
        JwtTokenService service = new JwtTokenService(SECRET, 900);
        var identity = service.validate(service.generate("user-1", "learner@example.com"));
        assertThat(identity.userId()).isEqualTo("user-1");
        assertThat(identity.email()).isEqualTo("learner@example.com");
        assertThat(service.expirationSeconds()).isEqualTo(900);
    }

    @Test
    void rejectsUnsafeSecretAndTokensWithoutRequiredClaims() {
        assertThatThrownBy(() -> new JwtTokenService("short", 10)).isInstanceOf(IllegalArgumentException.class);
        JwtTokenService service = new JwtTokenService(SECRET, 900);
        assertThatThrownBy(() -> service.validate(token(null, "user-1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(token("learner@example.com", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(token("learner@example.com", " ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void filterProtectsPrivatePathsAndAddsAuthenticatedAttributes() throws Exception {
        JwtTokenService tokenService = mock(JwtTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService, new ObjectMapper());

        MockHttpServletRequest missing = request("POST", "/api/auth/password-reset");
        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        filter.doFilter(missing, missingResponse, new MockFilterChain());
        assertThat(missingResponse.getStatus()).isEqualTo(401);
        assertThat(missingResponse.getContentAsString()).contains("valid Bearer token");

        when(tokenService.validate("bad")).thenThrow(new IllegalArgumentException("bad"));
        MockHttpServletRequest invalid = request("POST", "/api/auth/password-reset");
        invalid.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        MockHttpServletResponse invalidResponse = new MockHttpServletResponse();
        filter.doFilter(invalid, invalidResponse, new MockFilterChain());
        assertThat(invalidResponse.getContentAsString()).contains("invalid or expired");

        when(tokenService.validate("good"))
                .thenReturn(new JwtTokenService.AuthenticatedUser("user-1", "learner@example.com"));
        MockHttpServletRequest valid = request("POST", "/api/auth/password-reset");
        valid.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good");
        MockHttpServletResponse validResponse = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(valid, validResponse, chain);
        assertThat(valid.getAttribute("authenticatedUserId")).isEqualTo("user-1");
        assertThat(valid.getAttribute("authenticatedEmail")).isEqualTo("learner@example.com");
        assertThat(chain.getRequest()).isSameAs(valid);
    }

    @Test
    void filterSkipsLoginHealthAndInternalEndpoints() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtTokenService.class), new ObjectMapper());
        for (MockHttpServletRequest request : new MockHttpServletRequest[]{
                request("POST", "/api/auth/login"), request("GET", "/actuator/health/readiness"),
                request("POST", "/internal/credentials")}) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request, new MockHttpServletResponse(), chain);
            assertThat(chain.getRequest()).isSameAs(request);
        }
        assertThat(filter.shouldNotFilter(request("GET", "/api/auth/logout"))).isFalse();
    }

    @Test
    void exposesBcryptPasswordEncoderBean() {
        assertThat(new AuthConfiguration().passwordEncoder().matches("secret", 
                new AuthConfiguration().passwordEncoder().encode("secret"))).isTrue();
    }

    private MockHttpServletRequest request(String method, String path) {
        return new MockHttpServletRequest(method, path);
    }

    private String token(String subject, String userId) {
        var builder = Jwts.builder().expiration(Date.from(Instant.now().plusSeconds(60)));
        if (subject != null) builder.subject(subject);
        if (userId != null) builder.claim("userId", userId);
        return builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }
}
