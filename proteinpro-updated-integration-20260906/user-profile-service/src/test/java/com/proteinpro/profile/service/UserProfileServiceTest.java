package com.proteinpro.profile.service;

import com.proteinpro.profile.client.AuthenticationClient;
import com.proteinpro.profile.dto.ProfileDtos.RegistrationRequest;
import com.proteinpro.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.proteinpro.profile.kafka.UserProfileEventPublisher;
import com.proteinpro.profile.model.UserProfile;
import com.proteinpro.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import com.proteinpro.profile.web.ApiException;
import feign.FeignException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileServiceTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final AuthenticationClient client = mock(AuthenticationClient.class);
    private final UserProfileEventPublisher publisher = mock(UserProfileEventPublisher.class);
    private final UserProfileService service = new UserProfileService(repository, client, publisher, "internal-key");

    @Test
    void registrationStoresProfileWithoutPasswordAndPublishesIdentityEvent() {
        when(repository.existsByEmail("learner@example.com")).thenReturn(false);
        when(client.createCredential(eq("internal-key"), any())).thenReturn(ResponseEntity.status(201).build());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.register(new RegistrationRequest(
                "Ada", "Lovelace", "LEARNER@example.com", "password123"));

        assertThat(response.email()).isEqualTo("learner@example.com");
        assertThat(response.firstName()).isEqualTo("Ada");
        ArgumentCaptor<UserProfile> profile = ArgumentCaptor.forClass(UserProfile.class);
        verify(repository).save(profile.capture());
        verify(publisher).publishCreated(profile.getValue());
        assertThat(profile.getValue().getClass().getDeclaredFields())
                .noneMatch(field -> field.getName().toLowerCase().contains("password"));
    }

    @Test
    void rejectsDuplicateProfileBeforeCreatingCredentials() {
        when(repository.existsByEmail("learner@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegistrationRequest(
                "Ada", "Lovelace", " learner@example.com ", "password123")))
                .isInstanceOf(ApiException.class).hasMessage("A profile already exists for this email");
    }

    @Test
    void translatesCredentialConflictsAndUnavailableAuthenticationService() {
        RegistrationRequest request = new RegistrationRequest("Ada", "Lovelace", "learner@example.com", "password123");
        when(repository.existsByEmail("learner@example.com")).thenReturn(false);
        doThrow(mock(FeignException.Conflict.class)).when(client).createCredential(eq("internal-key"), any());
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(ApiException.class)
                .hasMessage("Credentials already exist for this email");

        org.mockito.Mockito.reset(client);
        doThrow(mock(FeignException.class)).when(client).createCredential(eq("internal-key"), any());
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(ApiException.class)
                .hasMessage("Authentication service is unavailable");
    }

    @Test
    void getsAndUpdatesOwnedProfile() {
        UserProfile profile = new UserProfile("user-1", " Ada ", " Lovelace ", "learner@example.com");
        when(repository.findById("user-1")).thenReturn(Optional.of(profile));
        when(repository.save(profile)).thenReturn(profile);

        assertThat(service.getById("user-1").email()).isEqualTo("learner@example.com");
        var updated = service.update("user-1", new UpdateProfileRequest("Grace ", " Hopper "));
        assertThat(updated.firstName()).isEqualTo("Grace");
        assertThat(updated.lastName()).isEqualTo("Hopper");
        verify(publisher).publishUpdated(profile);
    }

    @Test
    void missingProfileIsNotFoundForGetAndUpdate() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("missing")).isInstanceOf(ApiException.class)
                .hasMessage("User profile was not found");
        assertThatThrownBy(() -> service.update("missing", new UpdateProfileRequest("Ada", "Lovelace")))
                .isInstanceOf(ApiException.class).hasMessage("User profile was not found");
    }
}
