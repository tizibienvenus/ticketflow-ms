package com.camergo.document.infrastructure.config;

import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.repository.DocumentRepository;
import com.camergo.document.domain.service.DocumentEventPublisher;
import com.camergo.document.domain.service.DocumentValidationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Scheduler: scans and expires documents past their expiration date.
 *
 * IDEMPOTENCY STRATEGY:
 *   - Each document expiration is guarded by a Redis distributed lock key:
 *     "doc:expired:{documentId}" with TTL = 24h.
 *   - If a key already exists, the document was already processed → skip.
 *   - The domain model's expire() method is also idempotent (no-op if already EXPIRED).
 *
 * This ensures safety even if the scheduler runs concurrently (multiple pods).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentExpirationScheduler {

    private final DocumentRepository documentRepository;
    private final DocumentEventPublisher eventPublisher;
    private final DocumentValidationDomainService validationDomainService;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "doc:expired:";
    private static final Duration LOCK_TTL = Duration.ofHours(24);

    /**
     * Runs every hour. Can be tuned via property: scheduler.expiration.cron
     */
    @Scheduled(cron = "${scheduler.expiration.cron:0 0 * * * *}")
    public void processExpiredDocuments() {
        log.info("Expiration scheduler started at {}", Instant.now());

        List<Document> expired = documentRepository.findExpiredDocumentsNotYetProcessed(Instant.now());
        log.info("Found {} documents to expire", expired.size());

        int processed = 0;
        int skipped = 0;

        for (Document document : expired) {
            String lockKey = LOCK_PREFIX + document.getId();

            // Distributed idempotency lock - setIfAbsent is atomic
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
            if (Boolean.FALSE.equals(acquired)) {
                log.debug("Skipping already-processed expiration: documentId={}", document.getId());
                skipped++;
                continue;
            }

            try {
                document.expire(); // idempotent in domain too
                documentRepository.save(document);
                eventPublisher.publishDocumentExpired(document);

                boolean hasValidDocs = validationDomainService.hasValidDocuments(document.getUserId());
                eventPublisher.publishUserDocumentStatusUpdated(document.getUserId(), hasValidDocs);

                log.info("Document expired: documentId={} userId={}", document.getId(), document.getUserId());
                processed++;
            } catch (Exception e) {
                // Release lock on failure so next run can retry
                redisTemplate.delete(lockKey);
                log.error("Failed to expire documentId={}: {}", document.getId(), e.getMessage(), e);
            }
        }

        log.info("Expiration scheduler finished - processed={} skipped={}", processed, skipped);
    }
}
