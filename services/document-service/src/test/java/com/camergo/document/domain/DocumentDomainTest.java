package com.camergo.document.domain;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

class DocumentDomainTest {

    private Document buildPending() {
        return Document.builder()
                .id("doc-1").userId("user-1")
                .type(DocumentType.DRIVER_LICENSE)
                .status(DocumentStatus.PENDING)
                .fileUrl("http://file").fileName("f.jpg")
                .contentType("image/jpeg").fileSizeBytes(512)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .version(0).deleted(false).build();
    }

    @Test @DisplayName("verify(): transitions PENDING → VERIFIED, increments version")
    void verify_fromPending() {
        Document doc = buildPending();
        doc.verify();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(doc.getVersion()).isEqualTo(1);
    }

    @Test @DisplayName("verify(): throws if not PENDING")
    void verify_throwsIfNotPending() {
        Document doc = buildPending();
        doc.verify();
        assertThatThrownBy(doc::verify)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test @DisplayName("reject(): transitions PENDING → REJECTED, stores reason")
    void reject_fromPending() {
        Document doc = buildPending();
        doc.reject("Image too dark");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(doc.getRejectionReason()).isEqualTo("Image too dark");
        assertThat(doc.getVersion()).isEqualTo(1);
    }

    @Test @DisplayName("reject(): throws if reason is blank")
    void reject_blankReason() {
        Document doc = buildPending();
        assertThatThrownBy(() -> doc.reject(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test @DisplayName("expire(): idempotent — second call is a no-op")
    void expire_idempotent() {
        Document doc = buildPending();
        doc.expire();
        int versionAfterFirst = doc.getVersion();
        doc.expire(); // second call
        assertThat(doc.getVersion()).isEqualTo(versionAfterFirst); // no change
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.EXPIRED);
    }

    @Test @DisplayName("isExpired(): true when expirationDate is in the past")
    void isExpired_past() {
        Document doc = Document.builder()
                .id("d").userId("u").type(DocumentType.DRIVER_LICENSE)
                .status(DocumentStatus.VERIFIED).fileUrl("url").fileName("f")
                .contentType("image/jpeg").fileSizeBytes(1)
                .expirationDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .version(0).deleted(false).build();
        assertThat(doc.isExpired()).isTrue();
        assertThat(doc.isVerified()).isFalse(); // expired docs are not "valid"
    }

    @Test @DisplayName("isVerified(): false when status=VERIFIED but expired")
    void isVerified_expiredDoc() {
        Document doc = Document.builder()
                .id("d").userId("u").type(DocumentType.DRIVER_LICENSE)
                .status(DocumentStatus.VERIFIED).fileUrl("url").fileName("f")
                .contentType("image/jpeg").fileSizeBytes(1)
                .expirationDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .version(0).deleted(false).build();
        assertThat(doc.isVerified()).isFalse();
    }

    @Test @DisplayName("softDelete(): marks deleted, sets deletedBy and deletedAt")
    void softDelete() {
        Document doc = buildPending();
        doc.softDelete("admin-1");
        assertThat(doc.isDeleted()).isTrue();
        assertThat(doc.getDeletedBy()).isEqualTo("admin-1");
        assertThat(doc.getDeletedAt()).isNotNull();
    }
}
