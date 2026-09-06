package com.proteinpro.profile.security;

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
    void validatesIdentityAndRejectsUnsafeOrIncompleteTokens() {
        JwtTokenService service = new JwtTokenService(SECRET);
        var user = service.validate(token("learner@example.com", "user-1"));
        assertThat(user.userId()).isEqualTo("user-1");
        assertThat(user.email()).isEqualTo("learner@example.com");
        assertThatThrownBy(() -> new JwtTokenService("short")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(token(null, "user-1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(token("learner@example.com", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validate(token("learner@example.com", " ")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void filterProtectsProfileAndPopulatesIdentity() throws Exception {
        JwtTokenService tokens = mock(JwtTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens, new ObjectMapper());
        var missing = request("GET", "/api/profiles/me");
        var missingResponse = new MockHttpServletResponse();
        filter.doFilter(missing, missingResponse, new MockFilterChain());
        assertThat(missingResponse.getContentAsString()).contains("valid Bearer token");

        when(tokens.validate("bad")).thenThrow(new IllegalArgumentException("bad"));
        var invalid = request("GET", "/api/profiles/me");
        invalid.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        var invalidResponse = new MockHttpServletResponse();
        filter.doFilter(invalid, invalidResponse, new MockFilterChain());
        assertThat(invalidResponse.getStatus()).isEqualTo(401);
        assertThat(invalidResponse.getContentAsString()).contains("invalid or expired");

        when(tokens.validate("good")).thenReturn(new JwtTokenService.AuthenticatedUser("user-1", "learner@example.com"));
        var valid = request("GET", "/api/profiles/me");
        valid.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good");
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(valid, new MockHttpServletResponse(), chain);
        assertThat(valid.getAttribute("authenticatedUserId")).isEqualTo("user-1");
        assertThat(valid.getAttribute("authenticatedEmail")).isEqualTo("learner@example.com");
        assertThat(chain.getRequest()).isSameAs(valid);
    }

    @Test
    void filterSkipsRegistrationAndHealthOnly() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtTokenService.class), new ObjectMapper());
        for (MockHttpServletRequest request : new MockHttpServletRequest[]{
                request("POST", "/api/profiles/register"), request("GET", "/actuator/health")}) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request, new MockHttpServletResponse(), chain);
            assertThat(chain.getRequest()).isSameAs(request);
        }
        assertThat(filter.shouldNotFilter(request("GET", "/api/profiles/register"))).isFalse();
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
