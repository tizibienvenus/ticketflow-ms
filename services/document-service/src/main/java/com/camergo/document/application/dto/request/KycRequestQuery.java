package com.camergo.document.application.dto.request;

import com.camergo.document.domain.model.DocumentStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

import java.util.Set;

@Value
public class KycRequestQuery {

    /**
     * Statuses to include. Defaults to PENDING if not provided.
     * Allows: PENDING, REJECTED, EXPIRED, SUSPENDED — any combination.
     */
    @NotEmpty(message = "At least one status is required")
    Set<DocumentStatus> statuses;

    public static KycRequestQuery pendingOnly() {
        return new KycRequestQuery(Set.of(DocumentStatus.PENDING));
    }
}
