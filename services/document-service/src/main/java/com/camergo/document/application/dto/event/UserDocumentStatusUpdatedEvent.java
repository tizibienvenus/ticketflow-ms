package com.camergo.document.application.dto.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserDocumentStatusUpdatedEvent {
    String eventId;
    String userId;
    boolean hasValidDocuments;
    Instant occurredAt;
}
