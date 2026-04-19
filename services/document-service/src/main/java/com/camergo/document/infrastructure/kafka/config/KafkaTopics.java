package com.camergo.document.infrastructure.kafka.config;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String DOCUMENT_UPLOADED              = "document.uploaded";
    public static final String DOCUMENT_VERIFIED              = "document.verified";
    public static final String DOCUMENT_REJECTED              = "document.rejected";
    public static final String DOCUMENT_EXPIRED               = "document.expired";
    public static final String USER_DOCUMENT_STATUS_UPDATED   = "user.document.status.updated";

    // Dead Letter Queues
    public static final String DOCUMENT_VERIFIED_DLQ          = "document.verified.DLT";
    public static final String DOCUMENT_REJECTED_DLQ          = "document.rejected.DLT";
    public static final String DOCUMENT_EXPIRED_DLQ           = "document.expired.DLT";
}
