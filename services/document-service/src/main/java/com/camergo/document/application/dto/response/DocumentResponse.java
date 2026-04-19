package com.camergo.document.application.dto.response;

import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DocumentResponse {
    String id;
    String userId;
    DocumentType type;
    DocumentStatus status;
    String fileUrl;
    String fileName;
    String contentType;
    long fileSizeBytes;
    Instant expirationDate;
    Instant createdAt;
    Instant updatedAt;
    String rejectionReason;
    int version;
}
