package com.proteinpro.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proteinpro.auth.web.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(request, response, "A valid Bearer token is required");
            return;
        }
        try {
            JwtTokenService.AuthenticatedUser user = tokenService.validate(authorization.substring(7));
            request.setAttribute("authenticatedUserId", user.userId());
            request.setAttribute("authenticatedEmail", user.email());
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            writeUnauthorized(request, response, "The Bearer token is invalid or expired");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || (request.getMethod().equals("POST") && path.equals("/api/auth/login"))
                || path.startsWith("/internal/");
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(
                Instant.now().toString(), 401, "Unauthorized", message, request.getRequestURI()));
    }
}
