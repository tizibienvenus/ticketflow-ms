package com.camergo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;

/**
 * Core domain entity - pure Java, no framework annotations.
 * This is the heart of the bounded context.
 */
@Getter
@Builder
@With
@AllArgsConstructor
public class Document {

    private final String id;
    private final String userId;
    private final DocumentType type;
    private DocumentStatus status;
    private final String fileUrl;
    private final String fileName;
    private final String contentType;
    private final long fileSizeBytes;
    private final Instant expirationDate;
    private final Instant createdAt;
    private Instant updatedAt;
    private String rejectionReason;
    private int version;
    private boolean deleted;
    private String deletedBy;
    private Instant deletedAt;

    // ===================== Business Logic =====================

    public void verify() {
        assertStatus(DocumentStatus.PENDING, "Only PENDING documents can be verified.");
        this.status = DocumentStatus.VERIFIED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void reject(String reason) {
        assertStatus(DocumentStatus.PENDING, "Only PENDING documents can be rejected.");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason must not be blank.");
        }
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void expire() {
        if (this.status == DocumentStatus.EXPIRED) return; // idempotent
        this.status = DocumentStatus.EXPIRED;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return expirationDate != null && Instant.now().isAfter(expirationDate);
    }

    public boolean isVerified() {
        return DocumentStatus.VERIFIED.equals(this.status) && !isExpired();
    }

    private void assertStatus(DocumentStatus expected, String message) {
        if (!expected.equals(this.status)) {
            throw new IllegalStateException(message + " Current status: " + this.status);
        }
    }
}
