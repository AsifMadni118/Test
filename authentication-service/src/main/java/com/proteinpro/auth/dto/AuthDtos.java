package com.proteinpro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record CreateCredentialRequest(
            @NotBlank String userId,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds) {
    }

    public record PasswordResetRequest(
            @NotBlank @Size(min = 8, max = 72) String newPassword) {
    }
}
