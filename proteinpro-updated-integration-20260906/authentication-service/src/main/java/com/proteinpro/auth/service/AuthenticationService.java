package com.proteinpro.auth.service;

import com.proteinpro.auth.dto.AuthDtos.CreateCredentialRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginRequest;
import com.proteinpro.auth.dto.AuthDtos.LoginResponse;
import com.proteinpro.auth.kafka.AuthenticationEventPublisher;
import com.proteinpro.auth.model.Credential;
import com.proteinpro.auth.repository.CredentialRepository;
import com.proteinpro.auth.security.JwtTokenService;
import com.proteinpro.auth.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthenticationService {
    private final CredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final AuthenticationEventPublisher eventPublisher;

    public AuthenticationService(CredentialRepository repository, PasswordEncoder passwordEncoder,
                                 JwtTokenService tokenService, AuthenticationEventPublisher eventPublisher) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    public void createCredential(CreateCredentialRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmail(email) || repository.existsByUserId(request.userId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Credentials already exist for this user");
        }
        repository.save(new Credential(request.userId(), email, passwordEncoder.encode(request.password())));
    }

    public LoginResponse login(LoginRequest request) {
        Credential credential = repository.findByEmail(normalize(request.email()))
                .filter(Credential::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        String token = tokenService.generate(credential.getUserId(), credential.getEmail());
        eventPublisher.publishLogin(credential);
        return new LoginResponse(token, "Bearer", tokenService.expirationSeconds());
    }

    public void resetPassword(String authenticatedUserId, String newPassword) {
        Credential credential = repository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Credential was not found"));
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credential.setUpdatedAt(Instant.now());
        repository.save(credential);
        eventPublisher.publishPasswordReset(credential);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
