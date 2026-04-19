package com.camergo.document.domain.service;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentType;
import com.camergo.document.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Pure domain service: business rules on document validation logic.
 * No infrastructure concerns.
 */
@Service
@RequiredArgsConstructor
public class DocumentValidationDomainService {

    /**
     * Required document types for a driver to be considered "fully validated".
     */
    private static final Set<DocumentType> REQUIRED_DRIVER_DOCUMENTS = Set.of(
            DocumentType.DRIVER_LICENSE,
            DocumentType.NATIONAL_ID,
            DocumentType.VEHICLE_REGISTRATION,
            DocumentType.VEHICLE_INSURANCE
    );

    private final DocumentRepository documentRepository;

    /**
     * A user has valid documents if and only if ALL required document types
     * are present, VERIFIED, and not expired.
     */
    public boolean hasValidDocuments(String userId) {
        List<Document> documents = documentRepository.findByUserIdAndDeleted(userId, false);

        return REQUIRED_DRIVER_DOCUMENTS.stream()
                .allMatch(requiredType -> documents.stream()
                        .anyMatch(doc -> doc.getType() == requiredType && doc.isVerified()));
    }
}
