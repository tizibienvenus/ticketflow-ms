package com.camergo.document.infrastructure.persistence;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.KycRequest;
import com.camergo.document.domain.repository.KycRequestRepository;
import com.camergo.document.infrastructure.persistence.entity.DocumentEntity;
import com.camergo.document.infrastructure.persistence.mapper.DocumentEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MongoDB aggregation pipeline - groups documents by userId for any set of statuses.
 *
 * Pipeline:
 *  1. $match  - status IN [...], deleted = false
 *  2. $sort   - createdAt ASC (oldest first, ensures FIFO inside each group)
 *  3. $group  - by userId: collect matching docs, count, track min(createdAt)
 *  4. $sort   - oldestSubmissionDate ASC (priority order for admin queue)
 *  5. $facet  - paginated data + total user count (single DB round-trip)
 *
 * Scalability:
 *  - Compound index (status, deleted, createdAt) keeps $match O(log n)
 *  - Pagination is on users - page size is bounded even if one user has 100+ docs
 *  - $facet avoids a second COUNT query
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KycRequestAggregationAdapter implements KycRequestRepository {

    private final MongoTemplate mongoTemplate;
    private final DocumentEntityMapper entityMapper;

    @Override
    public Page<KycRequest> findGroupedByUser(Set<DocumentStatus> statuses, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            return Page.empty(pageable);
        }

        // Convert enum set → list of strings for MongoDB $in
        List<String> statusNames = statuses.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        // - Stage 1: filter by requested statuses, non-deleted ------
        MatchOperation match = Aggregation.match(
                Criteria.where("status").in(statusNames)
                        .and("deleted").is(false)
        );

        // - Stage 2: sort docs oldest-first (preserved inside $push) ---
        SortOperation sortDocs = Aggregation.sort(
                org.springframework.data.domain.Sort.by("createdAt").ascending()
        );

        // - Stage 3: group by userId -------------------
        GroupOperation group = Aggregation.group("userId")
                .push("$$ROOT").as("pendingDocuments")
                .count().as("totalPendingCount")
                .min("createdAt").as("oldestSubmissionDate");

        // - Stage 4: sort groups FIFO ------------------─
        SortOperation sortGroups = Aggregation.sort(
                org.springframework.data.domain.Sort.by("oldestSubmissionDate").ascending()
        );

        // - Stage 5: $facet - one round-trip for data + count ------─
        FacetOperation facet = Aggregation.facet(
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        ).as("data")
        .and(
                Aggregation.count().as("count")
        ).as("totalCount");

        Aggregation aggregation = Aggregation.newAggregation(
                match, sortDocs, group, sortGroups, facet
        );

        AggregationResults<org.bson.Document> raw =
                mongoTemplate.aggregate(aggregation, "documents", org.bson.Document.class);

        org.bson.Document result = raw.getUniqueMappedResult();
        if (result == null) {
            return Page.empty(pageable);
        }

        List<org.bson.Document> totalCountList = result.getList("totalCount", org.bson.Document.class);
        long total = (totalCountList == null || totalCountList.isEmpty())
                ? 0L
                : ((Number) totalCountList.get(0).get("count")).longValue();

        List<org.bson.Document> dataList = result.getList("data", org.bson.Document.class);
        List<KycRequest> kycRequests = dataList == null ? List.of() : dataList.stream()
                .map(this::toKycRequest)
                .toList();

        log.debug("KYC aggregation statuses={} page={} size={} totalUsers={}",
                statusNames, pageable.getPageNumber(), pageable.getPageSize(), total);

        return new PageImpl<>(kycRequests, pageable, total);
    }

    // - Private mapping helpers ---------------------─

    private KycRequest toKycRequest(org.bson.Document groupDoc) {
        String userId = groupDoc.getString("_id");
        int count = groupDoc.getInteger("totalPendingCount", 0);
        java.util.Date oldest = groupDoc.getDate("oldestSubmissionDate");
        java.time.Instant oldestInstant = oldest != null ? oldest.toInstant() : java.time.Instant.now();

        List<org.bson.Document> rawDocs = groupDoc.getList("pendingDocuments", org.bson.Document.class);
        List<Document> documents = rawDocs == null ? List.of() : rawDocs.stream()
                .map(this::bsonToEntity)
                .map(entityMapper::toDomain)
                .toList();

        return KycRequest.builder()
                .userId(userId)
                .totalPendingCount(count)
                .oldestSubmissionDate(oldestInstant)
                .pendingDocuments(documents)
                .build();
    }

    private DocumentEntity bsonToEntity(org.bson.Document d) {
        return DocumentEntity.builder()
                .id(/*d.getObjectId("_id") != null ? d.getObjectId("_id").toString() : */d.getString("_id"))
                .userId(d.getString("userId"))
                .type(parseEnum(com.camergo.document.domain.model.DocumentType.class, d.getString("type")))
                .status(parseEnum(DocumentStatus.class, d.getString("status")))
                .fileUrl(d.getString("fileUrl"))
                .fileName(d.getString("fileName"))
                .contentType(d.getString("contentType"))
                .fileSizeBytes(d.getLong("fileSizeBytes") != null ? d.getLong("fileSizeBytes") : 0L)
                .expirationDate(toInstant(d.getDate("expirationDate")))
                .createdAt(toInstant(d.getDate("createdAt")))
                .updatedAt(toInstant(d.getDate("updatedAt")))
                .rejectionReason(d.getString("rejectionReason"))
                .deleted(Boolean.TRUE.equals(d.getBoolean("deleted")))
                .build();
    }

    private String extractId(org.bson.Document doc) {
        Object idValue = doc.get("_id");
        if (idValue == null) return null;
        
        if (idValue instanceof org.bson.types.ObjectId) {
            return ((org.bson.types.ObjectId) idValue).toHexString();
        }
        
        if (idValue instanceof String) {
            return (String) idValue;
        }
        
        return idValue.toString();
    }

    private java.time.Instant toInstant(java.util.Date date) {
        return date != null ? date.toInstant() : null;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> clazz, String value) {
        if (value == null) return null;
        try { return Enum.valueOf(clazz, value); } catch (IllegalArgumentException e) { return null; }
    }
}
