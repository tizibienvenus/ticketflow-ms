package com.boaz.ticketflow.ticket.kafka;

import com.boaz.ticketflow.ticket.event.TicketCreatedEvent;
import com.boaz.ticketflow.ticket.event.TicketStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes domain events to Kafka topics.
 * Each publication is logged with the resulting offset on success, or the error on failure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.ticket-created}")
    private String ticketCreatedTopic;

    @Value("${kafka.topics.ticket-status-changed}")
    private String ticketStatusChangedTopic;

    public void publishTicketCreated(TicketCreatedEvent event) {
        String key = event.ticketId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(ticketCreatedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                    "[Kafka] Failed to publish TicketCreatedEvent for ticketId={}: {}",
                    event.ticketId(), 
                    ex.getMessage(),
                    ex
                );
            } else {
                log.info(
                    "[Kafka] Published TicketCreatedEvent | ticketId={} | topic={} | partition={} | offset={}",
                    event.ticketId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            }
        });
    }

    public void publishTicketStatusChanged(TicketStatusChangedEvent event) {
        String key = event.ticketId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(ticketStatusChangedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                    "[Kafka] Failed to publish TicketStatusChangedEvent for ticketId={}: {}",
                    event.ticketId(), 
                    ex.getMessage(),
                    ex
                );
            } else {
                log.info(
                    "[Kafka] Published TicketStatusChangedEvent | ticketId={} | {} → {} | topic={} | offset={}",
                    event.ticketId(),
                    event.previousStatus(),
                    event.newStatus(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().offset()
                );
            }
        });
    }
}