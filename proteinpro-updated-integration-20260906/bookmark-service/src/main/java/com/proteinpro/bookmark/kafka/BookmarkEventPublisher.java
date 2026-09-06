package com.proteinpro.bookmark.kafka;

import com.proteinpro.bookmark.model.Bookmark;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class BookmarkEventPublisher {
    private final KafkaTemplate<String, BookmarkEvent> kafkaTemplate;
    private final String createdTopic;
    private final String updatedTopic;
    private final String deletedTopic;

    public BookmarkEventPublisher(KafkaTemplate<String, BookmarkEvent> kafkaTemplate,
                                  @Value("${kafka.topics.bookmark-created}") String createdTopic,
                                  @Value("${kafka.topics.bookmark-updated}") String updatedTopic,
                                  @Value("${kafka.topics.bookmark-deleted}") String deletedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.createdTopic = createdTopic;
        this.updatedTopic = updatedTopic;
        this.deletedTopic = deletedTopic;
    }

    public void publishCreated(Bookmark bookmark) {
        send(createdTopic, "BOOKMARK_CREATED", bookmark);
    }

    public void publishUpdated(Bookmark bookmark) {
        send(updatedTopic, "BOOKMARK_UPDATED", bookmark);
    }

    public void publishDeleted(Bookmark bookmark) {
        send(deletedTopic, "BOOKMARK_DELETED", bookmark);
    }

    private void send(String topic, String type, Bookmark bookmark) {
        BookmarkEvent event = new BookmarkEvent(UUID.randomUUID().toString(), type, bookmark.getId(),
                bookmark.getUserId(), bookmark.getProteinId(), Instant.now());
        try {
            kafkaTemplate.send(topic, bookmark.getUserId(), event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka event publication was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Kafka event publication failed", exception);
        }
    }
}
