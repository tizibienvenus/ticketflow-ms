package com.camergo.document.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Admin-facing response: one entry per user, containing all their pending documents.
 */
@Value
@Builder
public class KycRequestResponse {
    String userId;
    int totalPendingCount;
    Instant oldestSubmissionDate;
    List<DocumentResponse> pendingDocuments;
}
