package com.camergo.document.application;

import com.camergo.document.application.dto.request.RejectDocumentRequest;
import com.camergo.document.application.dto.request.UploadDocumentRequest;
import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.service.DocumentApplicationService;
import com.camergo.document.application.service.DocumentNotFoundException;
import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import com.camergo.document.domain.repository.DocumentRepository;
import com.camergo.document.domain.repository.KycRequestRepository;
import com.camergo.document.domain.service.DocumentEventPublisher;
import com.camergo.document.domain.service.DocumentValidationDomainService;
import com.camergo.document.domain.service.FileStorageService;
import com.camergo.document.interfaces.mapper.DocumentMapper;
import com.camergo.document.interfaces.mapper.KycRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentApplicationServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock KycRequestRepository kycRequestRepository;
    @Mock FileStorageService fileStorageService;
    @Mock DocumentEventPublisher eventPublisher;
    @Mock DocumentValidationDomainService validationDomainService;
    @Mock DocumentMapper documentMapper;
    @Mock KycRequestMapper kycRequestMapper;

    @InjectMocks DocumentApplicationService service;

    private Document pendingDocument;

    @BeforeEach
    void setUp() {
        pendingDocument = Document.builder()
                .id("doc-001")
                .userId("user-001")
                .type(DocumentType.DRIVER_LICENSE)
                .status(DocumentStatus.PENDING)
                .fileUrl("http://storage/doc.jpg")
                .fileName("license.jpg")
                .contentType("image/jpeg")
                .fileSizeBytes(1024L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(0)
                .deleted(false)
                .build();
    }

    // ─── Upload ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("uploadDocument: stores file, saves doc, publishes event")
    void uploadDocument_success() {
        MockMultipartFile file = new MockMultipartFile("file", "license.jpg",
                "image/jpeg", new byte[1024]);
        UploadDocumentRequest request = UploadDocumentRequest.builder()
                .userId("user-001").type(DocumentType.DRIVER_LICENSE).file(file).build();

        when(fileStorageService.upload(any(), any())).thenReturn("http://storage/doc.jpg");
        when(documentRepository.save(any())).thenReturn(pendingDocument);
        when(documentMapper.toResponse(any())).thenReturn(mock(DocumentResponse.class));

        DocumentResponse result = service.uploadDocument(request);

        assertThat(result).isNotNull();
        verify(fileStorageService).upload(eq(file), anyString());
        verify(documentRepository).save(any());
        verify(eventPublisher).publishDocumentUploaded(any());
    }

    // ─── Verify ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("verifyDocument: sets VERIFIED, publishes verified + user status events")
    void verifyDocument_success() {
        when(documentRepository.findById("doc-001")).thenReturn(Optional.of(pendingDocument));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(validationDomainService.hasValidDocuments("user-001")).thenReturn(true);
        when(documentMapper.toResponse(any())).thenReturn(mock(DocumentResponse.class));

        service.verifyDocument("doc-001", "admin-001");

        assertThat(pendingDocument.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        verify(eventPublisher).publishDocumentVerified(pendingDocument);
        verify(eventPublisher).publishUserDocumentStatusUpdated("user-001", true);
    }

    @Test
    @DisplayName("verifyDocument: throws when document not found")
    void verifyDocument_notFound() {
        when(documentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.verifyDocument("missing", "admin-001"))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DisplayName("verifyDocument: throws when document already VERIFIED (double verify)")
    void verifyDocument_alreadyVerified() {
        Document alreadyVerified = pendingDocument.withStatus(DocumentStatus.VERIFIED);
        // Note: withStatus would need to be added — using domain method directly
        Document verified = Document.builder()
                .id("doc-001").userId("user-001").type(DocumentType.DRIVER_LICENSE)
                .status(DocumentStatus.VERIFIED).fileUrl("url").fileName("f.jpg")
                .contentType("image/jpeg").fileSizeBytes(1024).createdAt(Instant.now())
                .updatedAt(Instant.now()).version(1).deleted(false).build();

        when(documentRepository.findById("doc-001")).thenReturn(Optional.of(verified));

        assertThatThrownBy(() -> service.verifyDocument("doc-001", "admin-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    // ─── Reject ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejectDocument: sets REJECTED with reason, publishes events")
    void rejectDocument_success() {
        when(documentRepository.findById("doc-001")).thenReturn(Optional.of(pendingDocument));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(validationDomainService.hasValidDocuments("user-001")).thenReturn(false);
        when(documentMapper.toResponse(any())).thenReturn(mock(DocumentResponse.class));

        service.rejectDocument("doc-001",
                new RejectDocumentRequest("Photo is blurry and unreadable"), "admin-001");

        assertThat(pendingDocument.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(pendingDocument.getRejectionReason()).isEqualTo("Photo is blurry and unreadable");
        verify(eventPublisher).publishDocumentRejected(pendingDocument);
        verify(eventPublisher).publishUserDocumentStatusUpdated("user-001", false);
    }

    @Test
    @DisplayName("rejectDocument: throws when reason is blank")
    void rejectDocument_blankReason() {
        when(documentRepository.findById("doc-001")).thenReturn(Optional.of(pendingDocument));
        assertThatThrownBy(() ->
                service.rejectDocument("doc-001", new RejectDocumentRequest("  "), "admin-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rejection reason");
    }

    // ─── Soft delete ──────────────────────────────────────────────────────

    @Test
    @DisplayName("softDeleteDocument: marks deleted, does not publish Kafka event")
    void softDelete_success() {
        when(documentRepository.findById("doc-001")).thenReturn(Optional.of(pendingDocument));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDeleteDocument("doc-001", "user-001");

        assertThat(pendingDocument.isDeleted()).isTrue();
        assertThat(pendingDocument.getDeletedBy()).isEqualTo("user-001");
        verifyNoInteractions(eventPublisher);
    }
}
