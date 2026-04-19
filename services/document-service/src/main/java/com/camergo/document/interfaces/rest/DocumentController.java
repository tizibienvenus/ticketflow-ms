package com.camergo.document.interfaces.rest;

import com.boaz.ticketflow.common.wrappers.PageResponse;
import com.camergo.document.application.dto.request.KycRequestQuery;
import com.camergo.document.application.dto.request.RejectDocumentRequest;
import com.camergo.document.application.dto.request.UploadDocumentRequest;
import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.dto.response.KycRequestResponse;
import com.camergo.document.application.usecase.DocumentUseCase;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.DocumentType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.camergo.document.application.dto.request.UploadDocumentRequest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentApiDocs{

        private final DocumentUseCase documentUseCase;

        @Override
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<DocumentResponse> upload(
                @RequestPart("file") MultipartFile file,
                @RequestPart("userId") String userId,
                @RequestPart("type") String type,
                @RequestPart(value = "expirationDate", required = false) String expirationDate
        ) {

                UploadDocumentRequest request = UploadDocumentRequest.builder()
                        .userId(userId)
                        .type(DocumentType.valueOf(type))
                        .file(file)
                        .expirationDate(expirationDate != null ? Instant.parse(expirationDate) : null)
                        .build();

                DocumentResponse response = documentUseCase.uploadDocument(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Override
        @GetMapping("/kyc-request")
        //@PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<PageResponse<KycRequestResponse>> getKycRequests(
                @RequestParam(required = false) DocumentStatus status,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size
        ) {
                Pageable pageable = Pageable.ofSize(size).withPage(page);
                KycRequestQuery query = (status == null /*|| status.isEmpty()*/)
                        ? KycRequestQuery.pendingOnly()
                        : new KycRequestQuery(Set.of(status));

                return ResponseEntity.ok(documentUseCase.getKycRequests(query, pageable));
        }

        @Override
        @GetMapping("/{userId}")
        public ResponseEntity<List<DocumentResponse>> getByUserId(
                @PathVariable String userId,
                @AuthenticationPrincipal String authenticatedUserId) {

                // Users can only see their own documents; admins can see any
                // (Fine-grained check delegated to service in a full impl)
                List<DocumentResponse> docs = documentUseCase.getDocumentsByUserId(userId);
                return ResponseEntity.ok(docs);
        }

        @Override
        @PutMapping("/{id}/validate")
        //@PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<DocumentResponse> validate(
                @PathVariable String id,
                @AuthenticationPrincipal String adminId) {
                return ResponseEntity.ok(documentUseCase.verifyDocument(id, adminId));
        }

        @Override
        @PutMapping("/{id}/reject")
        //@PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<DocumentResponse> reject(
                @PathVariable String id,
                @Valid @RequestBody RejectDocumentRequest request,
                @AuthenticationPrincipal String adminId) {
                return ResponseEntity.ok(documentUseCase.rejectDocument(id, request, adminId));
        }

        @Override
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                @PathVariable String id,
                @AuthenticationPrincipal String userId) {
                documentUseCase.softDeleteDocument(id, userId);
                return ResponseEntity.noContent().build();
        }
}
