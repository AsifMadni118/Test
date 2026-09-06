package com.proteinpro.profile.service;

import com.proteinpro.profile.client.AuthenticationClient;
import com.proteinpro.profile.dto.ProfileDtos.CreateCredentialRequest;
import com.proteinpro.profile.dto.ProfileDtos.ProfileResponse;
import com.proteinpro.profile.dto.ProfileDtos.RegistrationRequest;
import com.proteinpro.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.proteinpro.profile.kafka.UserProfileEventPublisher;
import com.proteinpro.profile.model.UserProfile;
import com.proteinpro.profile.repository.UserProfileRepository;
import com.proteinpro.profile.web.ApiException;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;
    private final AuthenticationClient authenticationClient;
    private final UserProfileEventPublisher eventPublisher;
    private final String internalApiKey;

    public UserProfileService(UserProfileRepository repository, AuthenticationClient authenticationClient,
                              UserProfileEventPublisher eventPublisher,
                              @Value("${security.internal-api-key}") String internalApiKey) {
        this.repository = repository;
        this.authenticationClient = authenticationClient;
        this.eventPublisher = eventPublisher;
        this.internalApiKey = internalApiKey;
    }

    public ProfileResponse register(RegistrationRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "A profile already exists for this email");
        }

        String userId = UUID.randomUUID().toString();
        try {
            authenticationClient.createCredential(internalApiKey,
                    new CreateCredentialRequest(userId, email, request.password()));
        } catch (FeignException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Credentials already exist for this email");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Authentication service is unavailable");
        }

        UserProfile profile = repository.save(
                new UserProfile(userId, request.firstName().trim(), request.lastName().trim(), email));
        eventPublisher.publishCreated(profile);
        return toResponse(profile);
    }

    public ProfileResponse getById(String userId) {
        return repository.findById(userId).map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User profile was not found"));
    }

    public ProfileResponse update(String userId, UpdateProfileRequest request) {
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User profile was not found"));
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setUpdatedAt(Instant.now());
        UserProfile saved = repository.save(profile);
        eventPublisher.publishUpdated(saved);
        return toResponse(saved);
    }

    private ProfileResponse toResponse(UserProfile profile) {
        return new ProfileResponse(profile.getId(), profile.getFirstName(), profile.getLastName(),
                profile.getEmail(), profile.getCreatedAt(), profile.getUpdatedAt());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
