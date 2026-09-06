package com.proteinpro.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    private final JwtTokenService tokenService = mock(JwtTokenService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService, objectMapper);

    @Test
    void permitsEveryDocumentedPublicRouteAndRemovesSpoofedIdentityHeaders() {
        assertPublic(HttpMethod.OPTIONS, "/api/bookmarks");
        assertPublic(HttpMethod.GET, "/actuator/health/readiness");
        assertPublic(HttpMethod.POST, "/api/profiles/register");
        assertPublic(HttpMethod.POST, "/api/auth/login");
        assertPublic(HttpMethod.GET, "/api/proteins?source=whey");
    }

    @Test
    void rejectsMissingAndInvalidBearerTokensWithJsonResponse() {
        var missing = exchange(MockServerHttpRequest.get("/api/bookmarks").build());
        StepVerifier.create(filter.filter(missing, ignored -> Mono.error(new AssertionError("must not route"))))
                .verifyComplete();
        assertThat(missing.getResponse().getStatusCode().value()).isEqualTo(401);
        assertThat(body(missing)).contains("valid Bearer token is required");

        when(tokenService.validate("broken")).thenThrow(new IllegalArgumentException("bad token"));
        var invalid = exchange(MockServerHttpRequest.get("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer broken").build());
        StepVerifier.create(filter.filter(invalid, ignored -> Mono.error(new AssertionError("must not route"))))
                .verifyComplete();
        assertThat(body(invalid)).contains("invalid or expired");
    }

    @Test
    void validatesTokenAndPassesTrustedIdentityHeadersDownstream() {
        when(tokenService.validate("valid-token"))
                .thenReturn(new JwtTokenService.AuthenticatedUser("user-7", "alex@example.com"));
        var input = exchange(MockServerHttpRequest.get("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .header("X-Authenticated-User-Id", "spoofed")
                .header("X-Authenticated-User", "attacker@example.com").build());
        AtomicReference<ServerWebExchange> routed = new AtomicReference<>();

        StepVerifier.create(filter.filter(input, capture(routed))).verifyComplete();

        assertThat(routed.get().getRequest().getHeaders().getFirst("X-Authenticated-User-Id"))
                .isEqualTo("user-7");
        assertThat(routed.get().getRequest().getHeaders().getFirst("X-Authenticated-User"))
                .isEqualTo("alex@example.com");
        assertThat(filter.getOrder()).isEqualTo(-100);
    }

    @Test
    void fallsBackToMinimalJsonIfErrorSerializationFails() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("failure") { });
        JwtAuthenticationFilter failingFilter = new JwtAuthenticationFilter(tokenService, failingMapper);
        var exchange = exchange(MockServerHttpRequest.get("/api/bookmarks").build());

        StepVerifier.create(failingFilter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(body(exchange)).isEqualTo("{\"status\":401,\"error\":\"Unauthorized\"}");
    }

    private void assertPublic(HttpMethod method, String uri) {
        var request = MockServerHttpRequest.method(method, uri)
                .header("X-Authenticated-User-Id", "spoofed")
                .header("X-Authenticated-User", "spoofed@example.com").build();
        AtomicReference<ServerWebExchange> routed = new AtomicReference<>();
        StepVerifier.create(filter.filter(exchange(request), capture(routed))).verifyComplete();
        assertThat(routed.get().getRequest().getHeaders().containsKey("X-Authenticated-User-Id")).isFalse();
        assertThat(routed.get().getRequest().getHeaders().containsKey("X-Authenticated-User")).isFalse();
    }

    private GatewayFilterChain capture(AtomicReference<ServerWebExchange> routed) {
        return exchange -> {
            routed.set(exchange);
            return Mono.empty();
        };
    }

    private MockServerWebExchange exchange(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }

    private String body(MockServerWebExchange exchange) {
        return exchange.getResponse().getBodyAsString().block().getBytes(StandardCharsets.UTF_8).length == 0
                ? "" : exchange.getResponse().getBodyAsString().block();
    }
}
