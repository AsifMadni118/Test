package com.proteinpro.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final String USER_ID_HEADER = "X-Authenticated-User-Id";
    private static final String USER_EMAIL_HEADER = "X-Authenticated-User";

    private final JwtTokenService tokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest cleanRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_EMAIL_HEADER);
                }).build();
        ServerWebExchange cleanExchange = exchange.mutate().request(cleanRequest).build();

        if (isPublic(cleanRequest)) {
            return chain.filter(cleanExchange);
        }

        String authorization = cleanRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(cleanExchange, "A valid Bearer token is required");
        }

        try {
            JwtTokenService.AuthenticatedUser user = tokenService.validate(authorization.substring(7));
            ServerHttpRequest authenticatedRequest = cleanRequest.mutate()
                    .header(USER_ID_HEADER, user.userId())
                    .header(USER_EMAIL_HEADER, user.email())
                    .build();
            return chain.filter(cleanExchange.mutate().request(authenticatedRequest).build());
        } catch (RuntimeException exception) {
            return unauthorized(cleanExchange, "The Bearer token is invalid or expired");
        }
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        if (request.getMethod() == HttpMethod.OPTIONS
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/webjars")) {
            return true;
        }
        if (request.getMethod() == HttpMethod.POST
                && (path.equals("/api/profiles/register") || path.equals("/api/auth/login"))) {
            return true;
        }
        return request.getMethod() == HttpMethod.GET && path.startsWith("/api/proteins");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse body = new ErrorResponse(Instant.now().toString(), 401, "Unauthorized", message,
                exchange.getRequest().getURI().getPath());
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException exception) {
            byte[] bytes = "{\"status\":401,\"error\":\"Unauthorized\"}"
                    .getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private record ErrorResponse(String timestamp, int status, String error, String message, String path) {
    }
}
