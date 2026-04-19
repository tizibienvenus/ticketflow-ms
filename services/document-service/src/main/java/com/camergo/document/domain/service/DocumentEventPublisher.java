package com.camergo.document.domain.service;

import com.camergo.document.domain.model.Document;

/**
 * Domain-level event publisher port.
 * Implementations in infrastructure.kafka.
 */
public interface DocumentEventPublisher {

    void publishDocumentUploaded(Document document);

    void publishDocumentVerified(Document document);

    void publishDocumentRejected(Document document);

    void publishDocumentExpired(Document document);

    void publishUserDocumentStatusUpdated(String userId, boolean hasValidDocuments);
}
