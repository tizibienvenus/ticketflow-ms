package com.camergo.document.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import org.bson.Document;

/**
 * Programmatic index creation.
 *
 * Key indexes for the KYC aggregation pipeline:
 *
 *  1. (status, deleted, createdAt)  - covers $match + $sort in the pipeline
 *  2. (userId, status)              - covers per-user document lookups
 *  3. (expirationDate, status)      - covers the expiration scheduler query
 *
 * These make the aggregation O(log n) on the filtered set, not O(n) full scan.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureIndexes() {
        IndexOperations ops = mongoTemplate.indexOps("documents");

        // KYC aggregation: $match on status+deleted, then $sort on createdAt
        ops.ensureIndex(new CompoundIndexDefinition(
                new Document("status", 1).append("deleted", 1).append("createdAt", 1))
                .named("idx_status_deleted_createdAt"));

        // Per-user document fetch
        ops.ensureIndex(new CompoundIndexDefinition(
                new Document("userId", 1).append("status", 1))
                .named("idx_userId_status"));

        // Expiration scheduler
        ops.ensureIndex(new CompoundIndexDefinition(
                new Document("expirationDate", 1).append("status", 1).append("deleted", 1))
                .named("idx_expirationDate_status_deleted"));

        log.info("MongoDB indexes ensured for 'documents' collection.");
    }
}
