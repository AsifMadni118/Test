package com.proteinpro.bookmark.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    @Bean
    NewTopic bookmarkCreatedTopic(@Value("${kafka.topics.bookmark-created}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic bookmarkUpdatedTopic(@Value("${kafka.topics.bookmark-updated}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic bookmarkDeletedTopic(@Value("${kafka.topics.bookmark-deleted}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
