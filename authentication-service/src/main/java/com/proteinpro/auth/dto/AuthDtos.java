package com.proteinpro.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Authentication Data Transfer Objects")
public final class AuthDtos {
    private AuthDtos() {
    }

    @Schema(description = "Request payload to create user credentials internally")
    public record CreateCredentialRequest(
            @Schema(description = "Unique user identifier", example = "usr_12345")
            @NotBlank String userId,
            @Schema(description = "User email address", example = "user@example.com")
            @NotBlank @Email String email,
            @Schema(description = "Raw user password (min 8 characters)", example = "StrongP@ssw0rd")
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    @Schema(description = "Request payload for user login")
    public record LoginRequest(
            @Schema(description = "Registered user email address", example = "user@example.com")
            @NotBlank @Email String email,
            @Schema(description = "User password", example = "StrongP@ssw0rd")
            @NotBlank String password) {
    }

    @Schema(description = "Authentication token response payload")
    public record LoginResponse(
            @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String accessToken,
            @Schema(description = "Token type authorization scheme", example = "Bearer")
            String tokenType,
            @Schema(description = "Token lifetime in seconds", example = "1800")
            long expiresInSeconds) {
    }

    @Schema(description = "Request payload for resetting user password")
    public record PasswordResetRequest(
            @Schema(description = "New password (min 8 characters)", example = "NewStrongP@ssw0rd")
            @NotBlank @Size(min = 8, max = 72) String newPassword) {
    }
}

