package com.camergo.document.infrastructure.persistence.repository;

import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import com.camergo.document.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MongoDocumentRepository extends MongoRepository<DocumentEntity, String> {

    List<DocumentEntity> findByUserId(String userId);

    List<DocumentEntity> findByUserIdAndDeleted(String userId, boolean deleted);

    Page<DocumentEntity> findByStatus(DocumentStatus status, Pageable pageable);

    boolean existsByUserIdAndTypeAndStatus(String userId, DocumentType type, DocumentStatus status);

    /**
     * Find documents expired but not yet marked EXPIRED — used by scheduler.
     */
    @Query("{ 'expirationDate': { $lt: ?0 }, 'status': { $ne: 'EXPIRED' }, 'deleted': false }")
    List<DocumentEntity> findExpiredDocumentsNotYetProcessed(Instant now);
}
