package com.proteinpro.bookmark.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicConfigurationTest {
    @Test
    void declaresAllBookmarkLifecycleTopics() {
        KafkaTopicConfiguration configuration = new KafkaTopicConfiguration();
        assertThat(configuration.bookmarkCreatedTopic("bookmark-created").name()).isEqualTo("bookmark-created");
        assertThat(configuration.bookmarkUpdatedTopic("bookmark-updated").name()).isEqualTo("bookmark-updated");
        assertThat(configuration.bookmarkDeletedTopic("bookmark-deleted").name()).isEqualTo("bookmark-deleted");
    }
}
