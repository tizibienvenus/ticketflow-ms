package com.camergo.document.infrastructure.persistence;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import com.camergo.document.domain.repository.DocumentRepository;
import com.camergo.document.infrastructure.persistence.mapper.DocumentEntityMapper;
import com.camergo.document.infrastructure.persistence.repository.MongoDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adapter: bridges domain DocumentRepository port → MongoDB infrastructure.
 */
@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentRepository {

    private final MongoDocumentRepository mongoRepo;
    private final DocumentEntityMapper entityMapper;

    @Override
    public Document save(Document document) {
        return entityMapper.toDomain(mongoRepo.save(entityMapper.toEntity(document)));
    }

    @Override
    public Optional<Document> findById(String id) {
        return mongoRepo.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Document> findByUserId(String userId) {
        return mongoRepo.findByUserId(userId).stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public Page<Document> findByStatus(DocumentStatus status, Pageable pageable) {
        return mongoRepo.findByStatus(status, pageable).map(entityMapper::toDomain);
    }

    @Override
    public List<Document> findExpiredDocumentsNotYetProcessed(Instant now) {
        return mongoRepo.findExpiredDocumentsNotYetProcessed(now)
                .stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public boolean existsByUserIdAndTypeAndStatus(String userId, DocumentType type, DocumentStatus status) {
        return mongoRepo.existsByUserIdAndTypeAndStatus(userId, type, status);
    }

    @Override
    public List<Document> findByUserIdAndDeleted(String userId, boolean deleted) {
        return mongoRepo.findByUserIdAndDeleted(userId, deleted)
            .stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mongoRepo.deleteById(id);
    }
}
