package com.proteinpro.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ProfileDtos {
    private ProfileDtos() {
    }

    public record RegistrationRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName) {
    }

    public record ProfileResponse(
            String id,
            String firstName,
            String lastName,
            String email,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record CreateCredentialRequest(String userId, String email, String password) {
    }
}
