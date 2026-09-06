package com.proteinpro.profile.kafka;

import java.time.Instant;

public record UserProfileEvent(
        String eventId,
        String eventType,
        String userId,
        String email,
        Instant occurredAt) {
}
