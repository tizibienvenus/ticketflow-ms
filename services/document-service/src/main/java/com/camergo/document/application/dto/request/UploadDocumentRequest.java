package com.camergo.document.application.dto.request;

import com.camergo.document.domain.model.DocumentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Value
@Builder
public class UploadDocumentRequest {

    @NotBlank(message = "userId is required")
    String userId;

    @NotNull(message = "Document type is required")
    DocumentType type;

    @NotNull(message = "File is required")
    MultipartFile file;

    @Future(message = "Expiration date must be in the future")
    Instant expirationDate;
}
