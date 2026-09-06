package com.proteinpro.auth.kafka;

import com.proteinpro.auth.model.Credential;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationEventPublisherTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishesLoginAndPasswordResetToTheirConfiguredTopics() {
        KafkaTemplate<String, AuthenticationEvent> template = mock(KafkaTemplate.class);
        when(template.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        AuthenticationEventPublisher publisher = new AuthenticationEventPublisher(
                template, "authentication-events", "password-reset-events");
        Credential credential = new Credential("user-1", "alex@example.com", "hash");

        publisher.publishLogin(credential);
        publisher.publishPasswordReset(credential);

        verify(template).send(eq("authentication-events"), eq("user-1"), any(AuthenticationEvent.class));
        verify(template).send(eq("password-reset-events"), eq("user-1"), any(AuthenticationEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reportsFailedAndInterruptedKafkaPublication() throws Exception {
        KafkaTemplate<String, AuthenticationEvent> failedTemplate = mock(KafkaTemplate.class);
        when(failedTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));
        Credential credential = new Credential("user-1", "alex@example.com", "hash");
        AuthenticationEventPublisher failed = new AuthenticationEventPublisher(
                failedTemplate, "authentication-events", "password-reset-events");
        assertThatThrownBy(() -> failed.publishLogin(credential))
                .isInstanceOf(IllegalStateException.class).hasMessage("Kafka event publication failed");

        CompletableFuture<org.springframework.kafka.support.SendResult<String, AuthenticationEvent>> future =
                mock(CompletableFuture.class);
        when(future.get(org.mockito.ArgumentMatchers.anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("stop"));
        KafkaTemplate<String, AuthenticationEvent> interruptedTemplate = mock(KafkaTemplate.class);
        when(interruptedTemplate.send(any(), any(), any())).thenReturn(future);
        AuthenticationEventPublisher interrupted = new AuthenticationEventPublisher(
                interruptedTemplate, "authentication-events", "password-reset-events");
        assertThatThrownBy(() -> interrupted.publishPasswordReset(credential))
                .isInstanceOf(IllegalStateException.class).hasMessage("Kafka event publication was interrupted");
        assertThat(Thread.interrupted()).isTrue();
    }
}
