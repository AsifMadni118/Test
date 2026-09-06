package com.proteinpro.auth.kafka;

import com.proteinpro.auth.model.Credential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class AuthenticationEventPublisher {
    private final KafkaTemplate<String, AuthenticationEvent> kafkaTemplate;
    private final String authenticationTopic;
    private final String passwordResetTopic;

    public AuthenticationEventPublisher(KafkaTemplate<String, AuthenticationEvent> kafkaTemplate,
                                        @Value("${kafka.topics.authentication-events}") String authenticationTopic,
                                        @Value("${kafka.topics.password-reset-events}") String passwordResetTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.authenticationTopic = authenticationTopic;
        this.passwordResetTopic = passwordResetTopic;
    }

    public void publishLogin(Credential credential) {
        send(authenticationTopic, credential, event("USER_LOGGED_IN", credential));
    }

    public void publishPasswordReset(Credential credential) {
        send(passwordResetTopic, credential, event("PASSWORD_RESET_REQUESTED", credential));
    }

    private void send(String topic, Credential credential, AuthenticationEvent event) {
        try {
            kafkaTemplate.send(topic, credential.getUserId(), event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka event publication was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Kafka event publication failed", exception);
        }
    }

    private AuthenticationEvent event(String type, Credential credential) {
        return new AuthenticationEvent(UUID.randomUUID().toString(), type, credential.getId(),
                credential.getUserId(), credential.getEmail(), Instant.now());
    }
}
