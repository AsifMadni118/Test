package com.proteinpro.auth.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {
    @Bean
    NewTopic authenticationEventsTopic(@Value("${kafka.topics.authentication-events}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic passwordResetEventsTopic(@Value("${kafka.topics.password-reset-events}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic userCreatedTopic(@Value("${kafka.topics.user-created}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserCreatedEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(4000);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
        return factory;
    }
}
