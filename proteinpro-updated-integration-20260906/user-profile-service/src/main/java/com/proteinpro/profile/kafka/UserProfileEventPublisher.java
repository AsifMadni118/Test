package com.proteinpro.profile.kafka;

import com.proteinpro.profile.model.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class UserProfileEventPublisher {
    private final KafkaTemplate<String, UserProfileEvent> kafkaTemplate;
    private final String userCreatedTopic;
    private final String userUpdatedTopic;

    public UserProfileEventPublisher(KafkaTemplate<String, UserProfileEvent> kafkaTemplate,
                                     @Value("${kafka.topics.user-created}") String userCreatedTopic,
                                     @Value("${kafka.topics.user-updated}") String userUpdatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.userCreatedTopic = userCreatedTopic;
        this.userUpdatedTopic = userUpdatedTopic;
    }

    public void publishCreated(UserProfile profile) {
        send(userCreatedTopic, profile, event("USER_CREATED", profile));
    }

    public void publishUpdated(UserProfile profile) {
        send(userUpdatedTopic, profile, event("USER_PROFILE_UPDATED", profile));
    }

    private void send(String topic, UserProfile profile, UserProfileEvent event) {
        try {
            kafkaTemplate.send(topic, profile.getId(), event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka event publication was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Kafka event publication failed", exception);
        }
    }

    private UserProfileEvent event(String type, UserProfile profile) {
        return new UserProfileEvent(UUID.randomUUID().toString(), type, profile.getId(),
                profile.getEmail(), Instant.now());
    }
}
