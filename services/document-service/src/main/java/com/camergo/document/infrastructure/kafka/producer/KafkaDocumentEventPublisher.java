package com.camergo.document.infrastructure.kafka.producer;

import com.camergo.document.application.dto.event.*;
import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.service.DocumentEventPublisher;
import com.camergo.document.infrastructure.kafka.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter: implements domain DocumentEventPublisher port.
 * Uses userId as the Kafka partition key to preserve ordering per user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDocumentEventPublisher implements DocumentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishDocumentUploaded(Document document) {
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .documentId(document.getId())
            .userId(document.getUserId())
            .type(document.getType())
            .occurredAt(Instant.now())
            .build();
        send(KafkaTopics.DOCUMENT_UPLOADED, document.getUserId(), event);
    }

    @Override
    public void publishDocumentVerified(Document document) {
        DocumentVerifiedEvent event = DocumentVerifiedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .documentId(document.getId())
            .userId(document.getUserId())
            .type(document.getType())
            .occurredAt(Instant.now())
            .build();
        send(KafkaTopics.DOCUMENT_VERIFIED, document.getUserId(), event);
    }

    @Override
    public void publishDocumentRejected(Document document) {
        DocumentRejectedEvent event = DocumentRejectedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .documentId(document.getId())
            .userId(document.getUserId())
            .type(document.getType())
            .reason(document.getRejectionReason())
            .occurredAt(Instant.now())
            .build();
        send(KafkaTopics.DOCUMENT_REJECTED, document.getUserId(), event);
    }

    @Override
    public void publishDocumentExpired(Document document) {
        DocumentExpiredEvent event = DocumentExpiredEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .documentId(document.getId())
            .userId(document.getUserId())
            .type(document.getType())
            .expiredAt(document.getExpirationDate())
            .occurredAt(Instant.now())
            .build();
        send(KafkaTopics.DOCUMENT_EXPIRED, document.getUserId(), event);
    }

    @Override
    public void publishUserDocumentStatusUpdated(String userId, boolean hasValidDocuments) {
        UserDocumentStatusUpdatedEvent event = UserDocumentStatusUpdatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .userId(userId)
            .hasValidDocuments(hasValidDocuments)
            .occurredAt(Instant.now())
            .build();
        send(KafkaTopics.USER_DOCUMENT_STATUS_UPDATED, userId, event);
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic={} key={}: {}", topic, key, ex.getMessage());
            } else {
            log.debug("Event published: topic={} key={} offset={}",
                topic, key, result.getRecordMetadata().offset());
            }
        });
    }
}
