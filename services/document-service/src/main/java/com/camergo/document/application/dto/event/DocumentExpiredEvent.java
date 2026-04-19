package com.camergo.document.application.dto.event;

import com.camergo.document.domain.model.DocumentType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DocumentExpiredEvent {
    String eventId;
    String documentId;
    String userId;
    DocumentType type;
    Instant expiredAt;
    Instant occurredAt;
}
