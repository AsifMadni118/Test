package com.proteinpro.profile.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    @Bean
    NewTopic userCreatedTopic(@Value("${kafka.topics.user-created}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic userUpdatedTopic(@Value("${kafka.topics.user-updated}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
