package com.proteinpro.profile.kafka;

import com.proteinpro.profile.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileEventPublisherTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishesCreateAndUpdateEventsToTheirConfiguredTopics() {
        KafkaTemplate<String, UserProfileEvent> template = mock(KafkaTemplate.class);
        when(template.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        UserProfileEventPublisher publisher = new UserProfileEventPublisher(template, "user-created", "user-updated");
        UserProfile profile = new UserProfile("user-1", "Alex", "Morgan", "alex@example.com");

        publisher.publishCreated(profile);
        publisher.publishUpdated(profile);

        verify(template).send(eq("user-created"), eq("user-1"), any(UserProfileEvent.class));
        verify(template).send(eq("user-updated"), eq("user-1"), any(UserProfileEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reportsFailedAndInterruptedPublishing() throws Exception {
        UserProfile profile = new UserProfile("user-1", "Alex", "Morgan", "alex@example.com");
        KafkaTemplate<String, UserProfileEvent> failedTemplate = mock(KafkaTemplate.class);
        when(failedTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("down")));
        var failed = new UserProfileEventPublisher(failedTemplate, "user-created", "user-updated");
        assertThatThrownBy(() -> failed.publishCreated(profile)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka event publication failed");

        CompletableFuture<org.springframework.kafka.support.SendResult<String, UserProfileEvent>> future =
                mock(CompletableFuture.class);
        when(future.get(org.mockito.ArgumentMatchers.anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("stop"));
        KafkaTemplate<String, UserProfileEvent> interruptedTemplate = mock(KafkaTemplate.class);
        when(interruptedTemplate.send(any(), any(), any())).thenReturn(future);
        var interrupted = new UserProfileEventPublisher(interruptedTemplate, "user-created", "user-updated");
        assertThatThrownBy(() -> interrupted.publishUpdated(profile)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka event publication was interrupted");
        assertThat(Thread.interrupted()).isTrue();
    }
}
