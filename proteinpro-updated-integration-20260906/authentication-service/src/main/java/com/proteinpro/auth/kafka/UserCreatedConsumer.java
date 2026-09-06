package com.proteinpro.auth.kafka;

import com.proteinpro.auth.repository.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserCreatedConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCreatedConsumer.class);
    private final CredentialRepository repository;

    public UserCreatedConsumer(CredentialRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${kafka.topics.user-created}")
    public void handle(UserCreatedEvent event) {
        repository.findByUserId(event.userId()).ifPresentOrElse(credential -> {
            if (!credential.getEmail().equals(event.email())) {
                throw new IllegalStateException("Profile event email does not match credential email");
            }
            if (!credential.isActive()) {
                credential.setActive(true);
                credential.setUpdatedAt(Instant.now());
                repository.save(credential);
            }
            LOGGER.info("Activated credentials for userId={}", event.userId());
        }, () -> {
            throw new IllegalStateException("Credential must be created through Feign before UserCreated is consumed");
        });
    }
}
