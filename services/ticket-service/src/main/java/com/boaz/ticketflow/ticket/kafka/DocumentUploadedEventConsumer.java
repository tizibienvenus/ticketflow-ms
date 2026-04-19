package com.boaz.ticketflow.ticket.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.boaz.ticketflow.ticket.event.DocumentUploadedEvent;
import com.boaz.ticketflow.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes 'document.uploaded' events from document-service.
 * Automatically links the uploaded document to its associated ticket.
 *
 * Satisfies: Kafka event matrix - document.uploaded → ticket-service → liaison automatique au ticket.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadedEventConsumer {

    private final TicketRepository ticketRepository;

    @KafkaListener(
        topics = "${kafka.topics.document-uploaded}",
        groupId = "${spring.application.name}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onDocumentUploaded(
        @Payload DocumentUploadedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {

        log.info("[Kafka] Received DocumentUploadedEvent | documentId={} | ticketId={} | topic={} | partition={} | offset={}",
            event.documentId(), event.ticketId(), topic, partition, offset);

        if (event.ticketId() == null) {
            log.warn(
                "[Kafka] DocumentUploadedEvent has no ticketId — skipping automatic link. documentId={}",
                event.documentId()
            );
            return;
        }

        ticketRepository.findById(event.ticketId()).ifPresentOrElse(
            ticket -> {
                ticket.linkDocument(event.documentId());
                ticketRepository.save(ticket);
                log.info("[Kafka] Document {} automatically linked to ticket {}", event.documentId(), event.ticketId());
            },
            () -> log.warn(
                "[Kafka] Ticket {} not found — document {} could not be linked",
                event.ticketId(),
                event.documentId()
            )
        );
    }
}