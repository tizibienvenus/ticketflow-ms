package com.camergo.document.infrastructure.persistence.entity;

import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB persistence entity.
 * Kept separate from domain model to avoid coupling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id;

    @Indexed
    private String userId;

    private DocumentType type;

    @Indexed
    private DocumentStatus status;

    private String fileUrl;
    private String fileName;
    private String contentType;
    private long fileSizeBytes;

    @Indexed
    private Instant expirationDate;

    private Instant createdAt;
    private Instant updatedAt;
    private String rejectionReason;

    @Version
    private int version;

    @Indexed
    private boolean deleted;

    private String deletedBy;
    private Instant deletedAt;
}
