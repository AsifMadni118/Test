package com.proteinpro.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "User Profile Data Transfer Objects")
public final class ProfileDtos {
    private ProfileDtos() {
    }

    @Schema(description = "User registration request payload")
    public record RegistrationRequest(
            @Schema(description = "User first name", example = "John")
            @NotBlank @Size(max = 80) String firstName,
            @Schema(description = "User last name", example = "Doe")
            @NotBlank @Size(max = 80) String lastName,
            @Schema(description = "Unique user email address", example = "john.doe@example.com")
            @NotBlank @Email @Size(max = 254) String email,
            @Schema(description = "Account password (min 8 characters)", example = "P@ssw0rd123")
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    @Schema(description = "Profile update request payload")
    public record UpdateProfileRequest(
            @Schema(description = "Updated user first name", example = "Johnny")
            @NotBlank @Size(max = 80) String firstName,
            @Schema(description = "Updated user last name", example = "Doe")
            @NotBlank @Size(max = 80) String lastName) {
    }

    @Schema(description = "User profile response representation")
    public record ProfileResponse(
            @Schema(description = "Unique user profile identifier", example = "usr_12345")
            String id,
            @Schema(description = "User first name", example = "John")
            String firstName,
            @Schema(description = "User last name", example = "Doe")
            String lastName,
            @Schema(description = "User email address", example = "john.doe@example.com")
            String email,
            @Schema(description = "Account creation timestamp")
            Instant createdAt,
            @Schema(description = "Account last update timestamp")
            Instant updatedAt) {
    }

    @Schema(description = "Internal credential creation payload sent to Auth service")
    public record CreateCredentialRequest(
            @Schema(description = "User identifier") String userId,
            @Schema(description = "User email") String email,
            @Schema(description = "User password") String password) {
    }
}

