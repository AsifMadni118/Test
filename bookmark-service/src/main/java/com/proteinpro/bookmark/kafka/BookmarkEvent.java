package com.proteinpro.bookmark.kafka;

import java.time.Instant;

public record BookmarkEvent(
        String eventId,
        String eventType,
        String bookmarkId,
        String userId,
        String proteinId,
        Instant occurredAt) {
}
