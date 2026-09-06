package com.proteinpro.auth.kafka;

import java.time.Instant;

public record UserCreatedEvent(
        String eventId,
        String eventType,
        String userId,
        String email,
        Instant occurredAt) {
}
