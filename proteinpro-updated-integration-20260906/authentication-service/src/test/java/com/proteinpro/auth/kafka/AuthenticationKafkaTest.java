package com.proteinpro.auth.kafka;

import com.proteinpro.auth.model.Credential;
import com.proteinpro.auth.repository.CredentialRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationKafkaTest {
    @Test
    void declaresAllTopicsAndTypedRetryingListenerFactory() {
        KafkaConfiguration configuration = new KafkaConfiguration();
        NewTopic authentication = configuration.authenticationEventsTopic("authentication-events");
        NewTopic reset = configuration.passwordResetEventsTopic("password-reset-events");
        NewTopic created = configuration.userCreatedTopic("user-created");
        assertThat(authentication.name()).isEqualTo("authentication-events");
        assertThat(reset.name()).isEqualTo("password-reset-events");
        assertThat(created.name()).isEqualTo("user-created");

        @SuppressWarnings("unchecked")
        ConsumerFactory<String, UserCreatedEvent> consumers = mock(ConsumerFactory.class);
        var factory = configuration.kafkaListenerContainerFactory(consumers);
        assertThat(factory.getConsumerFactory()).isSameAs(consumers);
        assertThat(factory).isNotNull();
    }

    @Test
    void userCreatedEventActivatesMatchingCredentialOnlyOnce() {
        CredentialRepository repository = mock(CredentialRepository.class);
        UserCreatedConsumer consumer = new UserCreatedConsumer(repository);
        Credential inactive = new Credential("user-1", "learner@example.com", "hash");
        when(repository.findByUserId("user-1")).thenReturn(Optional.of(inactive));
        UserCreatedEvent event = new UserCreatedEvent("event-1", "USER_CREATED", "user-1",
                "learner@example.com", Instant.now());

        consumer.handle(event);
        assertThat(inactive.isActive()).isTrue();
        verify(repository).save(inactive);

        consumer.handle(event);
        verify(repository, org.mockito.Mockito.times(1)).save(inactive);
    }

    @Test
    void userCreatedEventRejectsMissingOrMismatchedCredential() {
        CredentialRepository repository = mock(CredentialRepository.class);
        UserCreatedConsumer consumer = new UserCreatedConsumer(repository);
        UserCreatedEvent event = new UserCreatedEvent("event-1", "USER_CREATED", "user-1",
                "learner@example.com", Instant.now());
        when(repository.findByUserId("user-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> consumer.handle(event)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Feign");

        Credential mismatch = new Credential("user-1", "other@example.com", "hash");
        when(repository.findByUserId("user-1")).thenReturn(Optional.of(mismatch));
        assertThatThrownBy(() -> consumer.handle(event)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
        verify(repository, never()).save(mismatch);
    }
}
