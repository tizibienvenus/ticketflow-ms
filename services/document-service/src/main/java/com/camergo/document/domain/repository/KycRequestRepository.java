package com.camergo.document.domain.repository;

import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.KycRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * Read-model repository for KYC aggregation.
 * Follows CQRS: queries don't go through the write-side DocumentRepository.
 */
public interface KycRequestRepository {

    /**
     * Returns one KycRequest per user who has at least one document
     * matching ANY of the given statuses.
     *
     * Each KycRequest contains only the documents that match the requested statuses
     * - other documents of the same user are excluded.
     *
     * Sorted by oldest submission date (FIFO).
     * Paginated on users - bounded page size regardless of document volume.
     *
     * Examples:
     *   findGroupedByUser(Set.of(PENDING), pageable)           → KYC review queue
     *   findGroupedByUser(Set.of(REJECTED), pageable)          → rejected docs
     *   findGroupedByUser(Set.of(PENDING, REJECTED), pageable) → all unresolved
     *   findGroupedByUser(Set.of(EXPIRED), pageable)           → expired docs audit
     */
    Page<KycRequest> findGroupedByUser(Set<DocumentStatus> statuses, Pageable pageable);
}
