package com.proteinpro.auth.service;

import com.proteinpro.auth.dto.AuthDtos.LoginRequest;
import com.proteinpro.auth.dto.AuthDtos.CreateCredentialRequest;
import com.proteinpro.auth.kafka.AuthenticationEventPublisher;
import com.proteinpro.auth.model.Credential;
import com.proteinpro.auth.repository.CredentialRepository;
import com.proteinpro.auth.security.JwtTokenService;
import com.proteinpro.auth.web.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {
    private final CredentialRepository repository = mock(CredentialRepository.class);
    private final AuthenticationEventPublisher publisher = mock(AuthenticationEventPublisher.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtTokenService tokens = new JwtTokenService(
            "01234567890123456789012345678901", 1800);
    private final AuthenticationService service =
            new AuthenticationService(repository, encoder, tokens, publisher);

    @Test
    void logsInActiveCredentialWithoutExposingPassword() {
        Credential credential = new Credential("user-1", "learner@example.com", encoder.encode("password123"));
        credential.setActive(true);
        when(repository.findByEmail("learner@example.com")).thenReturn(Optional.of(credential));

        var response = service.login(new LoginRequest("LEARNER@example.com", "password123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(publisher).publishLogin(credential);
    }

    @Test
    void rejectsWrongPassword() {
        Credential credential = new Credential("user-1", "learner@example.com", encoder.encode("password123"));
        credential.setActive(true);
        when(repository.findByEmail("learner@example.com")).thenReturn(Optional.of(credential));

        assertThatThrownBy(() -> service.login(new LoginRequest("learner@example.com", "wrong")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void createsNormalizedBcryptCredential() {
        when(repository.existsByEmail("learner@example.com")).thenReturn(false);
        when(repository.existsByUserId("user-1")).thenReturn(false);

        service.createCredential(new CreateCredentialRequest("user-1", " LEARNER@example.com ", "password123"));

        var captor = org.mockito.ArgumentCaptor.forClass(Credential.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("learner@example.com");
        assertThat(encoder.matches("password123", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmailOrUserId() {
        when(repository.existsByEmail("duplicate@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.createCredential(new CreateCredentialRequest(
                "user-1", "duplicate@example.com", "password123")))
                .isInstanceOf(ApiException.class).hasMessageContaining("already exist");
        verify(repository, never()).existsByUserId("user-1");

        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(repository.existsByUserId("user-2")).thenReturn(true);
        assertThatThrownBy(() -> service.createCredential(new CreateCredentialRequest(
                "user-2", "new@example.com", "password123")))
                .isInstanceOf(ApiException.class).hasMessageContaining("already exist");
    }

    @Test
    void rejectsMissingOrInactiveCredential() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        Credential inactive = new Credential("user-2", "inactive@example.com", encoder.encode("password123"));
        when(repository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "password123")))
                .isInstanceOf(ApiException.class).hasMessage("Invalid email or password");
        assertThatThrownBy(() -> service.login(new LoginRequest("inactive@example.com", "password123")))
                .isInstanceOf(ApiException.class).hasMessage("Invalid email or password");
    }

    @Test
    void resetsPasswordAndPublishesEvent() {
        Credential credential = new Credential("user-1", "learner@example.com", encoder.encode("oldpassword"));
        when(repository.findByUserId("user-1")).thenReturn(Optional.of(credential));

        service.resetPassword("user-1", "newpassword");

        assertThat(encoder.matches("newpassword", credential.getPasswordHash())).isTrue();
        assertThat(credential.getUpdatedAt()).isNotNull();
        verify(repository).save(credential);
        verify(publisher).publishPasswordReset(credential);
        assertThat(tokens.expirationSeconds()).isEqualTo(1800);
    }

    @Test
    void reportsMissingCredentialDuringPasswordReset() {
        when(repository.findByUserId("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resetPassword("missing", "newpassword"))
                .isInstanceOf(ApiException.class).hasMessage("Credential was not found");
    }
}
