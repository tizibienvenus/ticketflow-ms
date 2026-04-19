package com.camergo.document.application.usecase;

import com.boaz.ticketflow.common.wrappers.PageResponse;
import com.camergo.document.application.dto.request.KycRequestQuery;
import com.camergo.document.application.dto.request.RejectDocumentRequest;
import com.camergo.document.application.dto.request.UploadDocumentRequest;
import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.dto.response.KycRequestResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Application use case interface.
 * Defines operations callable from the REST layer.
 */
public interface DocumentUseCase {

    DocumentResponse uploadDocument(UploadDocumentRequest request);

    DocumentResponse getDocumentById(String id);

    List<DocumentResponse> getDocumentsByUserId(String userId);

    /**
     * Returns paginated KYC requests grouped by user, filtered by the given statuses.
     * One entry per user - contains only documents matching the requested statuses.
     * Sorted FIFO by oldest submission date.
     *
     * @param query  statuses to filter on (e.g. PENDING, REJECTED, EXPIRED)
     * @param pageable pagination params
     */
    PageResponse<KycRequestResponse> getKycRequests(KycRequestQuery query, Pageable pageable);

    DocumentResponse verifyDocument(String documentId, String adminId);

    DocumentResponse rejectDocument(String documentId, RejectDocumentRequest request, String adminId);

    void softDeleteDocument(String documentId, String deletedBy);
}
