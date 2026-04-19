package com.camergo.document.domain.repository;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository port (interface in domain layer).
 * Implementations live in infrastructure.persistence.
 */
public interface DocumentRepository {

    Document save(Document document);

    Optional<Document> findById(String id);

    List<Document> findByUserId(String userId);

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    /**
     * Find documents whose expiration date has passed and are not yet marked EXPIRED.
     */
    List<Document> findExpiredDocumentsNotYetProcessed(Instant now);

    boolean existsByUserIdAndTypeAndStatus(String userId, DocumentType type, DocumentStatus status);

    List<Document> findByUserIdAndDeleted(String userId, boolean deleted);

    void deleteById(String id);
}
