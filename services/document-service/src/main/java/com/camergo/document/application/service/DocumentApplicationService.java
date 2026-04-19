package com.camergo.document.application.service;

import com.boaz.ticketflow.common.wrappers.PageResponse;
import com.camergo.document.application.dto.request.KycRequestQuery;
import com.camergo.document.application.dto.request.RejectDocumentRequest;
import com.camergo.document.application.dto.request.UploadDocumentRequest;
import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.dto.response.KycRequestResponse;
import com.camergo.document.application.usecase.DocumentUseCase;
import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.repository.DocumentRepository;
import com.camergo.document.domain.repository.KycRequestRepository;
import com.camergo.document.domain.service.DocumentEventPublisher;
import com.camergo.document.domain.service.DocumentValidationDomainService;
import com.camergo.document.domain.service.FileStorageService;
import com.camergo.document.interfaces.mapper.DocumentMapper;
import com.camergo.document.interfaces.mapper.KycRequestMapper;
import com.camergo.document.domain.model.KycRequest;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApplicationService implements DocumentUseCase {

    private final DocumentRepository documentRepository;
    private final KycRequestRepository kycRequestRepository;
    private final FileStorageService fileStorageService;
    private final DocumentEventPublisher eventPublisher;
    private final DocumentValidationDomainService validationDomainService;
    private final DocumentMapper documentMapper;
    private final KycRequestMapper kycRequestMapper;

    @Override
    public DocumentResponse uploadDocument(UploadDocumentRequest request) {
        log.info("Uploading document for userId={}, type={}", request.getUserId(), request.getType());

        String objectKey = buildObjectKey(request.getUserId(), request.getType().name(),
            request.getFile().getOriginalFilename());

        //String fileUrl = fileStorageService.upload(request.getFile(), objectKey);

        Document document = Document.builder()
            .id(UUID.randomUUID().toString())
            .userId(request.getUserId())
            .type(request.getType())
            .status(DocumentStatus.PENDING)
            //.fileUrl(fileUrl)
            .fileName(request.getFile().getOriginalFilename())
            .contentType(request.getFile().getContentType())
            .fileSizeBytes(request.getFile().getSize())
            .expirationDate(request.getExpirationDate())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .version(0)
            .deleted(false)
            .build();

        Document saved = documentRepository.save(document);
        eventPublisher.publishDocumentUploaded(saved);

        log.info("Document uploaded successfully: documentId={}", saved.getId());
        return documentMapper.toResponse(saved);
    }

    @Override
    public DocumentResponse getDocumentById(String id) {
        Document document = findDocumentOrThrow(id);
        return documentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getDocumentsByUserId(String userId) {
        return documentRepository.findByUserIdAndDeleted(userId, false)
            .stream()
            .map(documentMapper::toResponse)
            .toList();
    }

    @Override
    public PageResponse<KycRequestResponse> getKycRequests(KycRequestQuery query, Pageable pageable) {
        // 1. Récupérer la page de KycRequest depuis le repository
        Page<KycRequest> kycRequestPage = kycRequestRepository.findGroupedByUser(query.getStatuses(), pageable);
        
        // 2. Convertir le contenu en KycRequestResponse
        List<KycRequestResponse> content = kycRequestPage.getContent()
            .stream()
            .map(kycRequestMapper::toResponse)
            .collect(Collectors.toList());
        
        // 3. Utiliser la méthode statique of() pour créer le PageResponse
        return PageResponse.of(content, kycRequestPage);
    }

    @Override
    public DocumentResponse verifyDocument(String documentId, String adminId) {
        log.info("Admin {} verifying document {}", adminId, documentId);

        Document document = findDocumentOrThrow(documentId);
        document.verify();

        Document saved = documentRepository.save(document);

        eventPublisher.publishDocumentVerified(saved);

        boolean hasValidDocs = validationDomainService.hasValidDocuments(saved.getUserId());
        eventPublisher.publishUserDocumentStatusUpdated(saved.getUserId(), hasValidDocs);

        log.info("Document verified: documentId={}, userId={}", documentId, saved.getUserId());
        return documentMapper.toResponse(saved);
    }

    @Override
    public DocumentResponse rejectDocument(String documentId, RejectDocumentRequest request, String adminId) {
        log.info("Admin {} rejecting document {} - reason: {}", adminId, documentId, request.getReason());

        Document document = findDocumentOrThrow(documentId);
        document.reject(request.getReason());

        Document saved = documentRepository.save(document);

        eventPublisher.publishDocumentRejected(saved);

        boolean hasValidDocs = validationDomainService.hasValidDocuments(saved.getUserId());
        eventPublisher.publishUserDocumentStatusUpdated(saved.getUserId(), hasValidDocs);

        log.info("Document rejected: documentId={}, userId={}", documentId, saved.getUserId());
        return documentMapper.toResponse(saved);
    }

    @Override
    public void softDeleteDocument(String documentId, String deletedBy) {
        Document document = findDocumentOrThrow(documentId);
        document.softDelete(deletedBy);
        documentRepository.save(document);
        log.info("Document soft-deleted: documentId={}, by={}", documentId, deletedBy);
    }

    // ===================== Private helpers =====================

    private Document findDocumentOrThrow(String id) {
        return documentRepository.findById(id)
            .filter(doc -> !doc.isDeleted())
            .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + id));
    }

    private String buildObjectKey(String userId, String type, String originalFilename) {
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf('.'))
            : "";
        return String.format("documents/%s/%s/%s%s", userId, type, UUID.randomUUID(), extension);
    }
}
