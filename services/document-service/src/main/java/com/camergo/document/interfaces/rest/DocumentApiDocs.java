package com.camergo.document.interfaces.rest;

import com.boaz.ticketflow.common.wrappers.PageResponse;
import com.camergo.document.application.dto.request.KycRequestQuery;
import com.camergo.document.application.dto.request.RejectDocumentRequest;
import com.camergo.document.application.dto.request.UploadDocumentRequest;
import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.dto.response.KycRequestResponse;
import com.camergo.document.domain.model.DocumentStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.method.P;

@Tag(name = "Documents", description = "Driver & user document management")
public interface DocumentApiDocs {

    @Operation(
        summary = "Upload a document",
        description = "Uploads a file and stores metadata. Status starts as PENDING.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    ResponseEntity<DocumentResponse> upload(
        @RequestPart("file") MultipartFile file,
        @RequestPart("userId") String userId,
        @RequestPart("type") String type,
        @RequestPart(value = "expirationDate", required = false) String expirationDate
    );

    @Operation(
        summary = "Get KYC requests grouped by user (admin)",
        description = """
                Returns paginated KYC requests **grouped by user**, filtered by status.
                One entry per user - contains only documents matching the requested statuses.
                Sorted FIFO by oldest submission date.

                **Usage examples:**
                - `?statuses=PENDING`                  → review queue
                - `?statuses=REJECTED`                 → rejected documents
                - `?statuses=PENDING&statuses=REJECTED`→ all unresolved
                - `?statuses=EXPIRED`                  → expired documents audit

            Defaults to `PENDING` when no status is provided.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Paginated KYC requests grouped by user"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
        }
    )
    ResponseEntity<PageResponse<KycRequestResponse>> getKycRequests(
        @Parameter(description = "Document statuses to filter on. Repeatable. Defaults to PENDING.")
        @RequestParam(required = false) DocumentStatus status,
        
        @Parameter(description = "Pagination parameters. Default page=0, size=20.")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of records per page. Default is 20.")
        @RequestParam(defaultValue = "20") int size
    );

    @Operation(
        summary = "Get all documents for a user",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of documents"),
            @ApiResponse(responseCode = "404", description = "User has no documents")
        }
    )
    ResponseEntity<List<DocumentResponse>> getByUserId(
        @PathVariable String userId,
        String authenticatedUserId
    );

    @Operation(
        summary = "Validate a document (admin)",
        description = "Marks document as VERIFIED. Triggers Kafka events.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Document verified"),
            @ApiResponse(responseCode = "409", description = "Document not in PENDING state"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
        }
    )
    ResponseEntity<DocumentResponse> validate(
        @PathVariable String id,
        String adminId
    );

    @Operation(
        summary = "Reject a document (admin)",
        description = "Marks document as REJECTED with a mandatory reason. Triggers Kafka events.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(
                value = """
                    {
                        "reason": "Photo is blurry and cannot be validated"
                    }
                    """
                ))
            ),
            responses = {
                @ApiResponse(responseCode = "200", description = "Document rejected"),
                @ApiResponse(responseCode = "409", description = "Document not in PENDING state"),
                @ApiResponse(responseCode = "403", description = "Forbidden - admin only")
            }
    )
    ResponseEntity<DocumentResponse> reject(
        @PathVariable String id,
        @Valid @RequestBody RejectDocumentRequest request,
        String adminId
    );

    @Operation(summary = "Soft-delete a document")
    ResponseEntity<Void> delete(
        @PathVariable String id,
        String userId
    );
}