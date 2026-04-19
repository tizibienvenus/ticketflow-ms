package com.boaz.ticketflow.ticket.feign;

import com.boaz.ticketflow.ticket.feign.dto.DocumentMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;



/**
 * Fallback factory for {@link DocumentServiceClient}.
 * Logs the degraded call and returns null — callers must handle gracefully.
 */
/* @Component
@Slf4j
public class DocumentServiceClientFallbackFactory implements FallbackFactory<DocumentServiceClient> {

    @Override
    public DocumentServiceClient create(Throwable cause) {
        return new DocumentServiceClient() {
            @Override
            public DocumentMetadataResponse getDocumentById(String documentId) {
                log.warn("[FALLBACK] document-service unavailable for documentId={}, cause: {}",
                        documentId, cause.getMessage());
                return null;
            }
        };
    }
} */