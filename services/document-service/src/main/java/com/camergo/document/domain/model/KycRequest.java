package com.camergo.document.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Read model: groups all PENDING documents of a single user.
 * Used for admin KYC review — one entry per user, not per document.
 */
@Getter
@Builder
public class KycRequest {

    private final String userId;
    private final List<Document> pendingDocuments;
    private final int totalPendingCount;
    private final Instant oldestSubmissionDate; // for priority sorting
}
