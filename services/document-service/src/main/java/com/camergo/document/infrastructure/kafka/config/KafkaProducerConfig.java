package com.camergo.document.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!dev")
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Idempotent producer — exactly-once semantics at producer level
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =================== Topic Declarations ===================

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_UPLOADED).partitions(3).replicas(2).build();
    }

    @Bean
    public NewTopic documentVerifiedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_VERIFIED).partitions(3).replicas(2).build();
    }

    @Bean
    public NewTopic documentRejectedTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_REJECTED).partitions(3).replicas(2).build();
    }

    @Bean
    public NewTopic documentExpiredTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_EXPIRED).partitions(3).replicas(2).build();
    }

    @Bean
    public NewTopic userDocumentStatusUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.USER_DOCUMENT_STATUS_UPDATED).partitions(3).replicas(2).build();
    }

    // Dead Letter Topics
    @Bean
    public NewTopic documentVerifiedDltTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_VERIFIED_DLQ).partitions(1).replicas(2).build();
    }

    @Bean
    public NewTopic documentRejectedDltTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_REJECTED_DLQ).partitions(1).replicas(2).build();
    }

    @Bean
    public NewTopic documentExpiredDltTopic() {
        return TopicBuilder.name(KafkaTopics.DOCUMENT_EXPIRED_DLQ).partitions(1).replicas(2).build();
    }
}
