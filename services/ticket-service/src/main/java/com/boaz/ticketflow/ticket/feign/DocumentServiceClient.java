package com.boaz.ticketflow.ticket.feign;

import com.boaz.ticketflow.ticket.feign.dto.DocumentMetadataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



/**
 * Declarative Feign client for communication with document-service.
 * JWT is propagated automatically via {@link FeignJwtRequestInterceptor}.
 */
/* @FeignClient(
    name = "document-service",
    fallbackFactory = DocumentServiceClientFallbackFactory.class
)
public interface DocumentServiceClient {

    @GetMapping("/api/documents/{id}")
    DocumentMetadataResponse getDocumentById(@PathVariable("id") String documentId);
} */