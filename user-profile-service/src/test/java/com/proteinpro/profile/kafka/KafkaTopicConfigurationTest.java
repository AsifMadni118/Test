package com.proteinpro.profile.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicConfigurationTest {
    @Test
    void declaresProfileLifecycleTopics() {
        KafkaTopicConfiguration configuration = new KafkaTopicConfiguration();
        assertThat(configuration.userCreatedTopic("user-created").name()).isEqualTo("user-created");
        assertThat(configuration.userUpdatedTopic("user-updated").name()).isEqualTo("user-updated");
    }
}
