package com.proteinpro.auth.kafka;

import java.time.Instant;

public record AuthenticationEvent(
        String eventId,
        String eventType,
        String credentialId,
        String userId,
        String email,
        Instant occurredAt) {
}
