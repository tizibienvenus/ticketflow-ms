package com.boaz.ticketflow.ticket.feign.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Minimal document metadata returned by document-service.
 */
public record DocumentMetadataResponse(
        UUID id,
        String fileName,
        String mimeType,
        long sizeBytes,
        OffsetDateTime uploadedAt
) {}