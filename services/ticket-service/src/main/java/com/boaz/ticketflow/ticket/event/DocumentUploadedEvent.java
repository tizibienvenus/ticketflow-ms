package com.boaz.ticketflow.ticket.event;

import java.time.OffsetDateTime;

/**
 * Event consumed from Kafka topic 'document.uploaded', produced by document-service.
 * When received, ticket-service automatically links the document to its ticket.
 */
public record DocumentUploadedEvent(
    String documentId,
    String ticketId,
    String fileName,
    String mimeType,
    long sizeBytes,
    OffsetDateTime uploadedAt
) {}