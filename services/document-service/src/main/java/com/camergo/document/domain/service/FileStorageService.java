package com.camergo.document.domain.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Storage abstraction port. Swappable: MinIO (dev) / S3 (prod).
 */
public interface FileStorageService {

    /**
     * Upload file and return its public/presigned URL.
     */
    String upload(MultipartFile file, String objectKey);

    /**
     * Delete file from storage.
     */
    void delete(String objectKey);

    /**
     * Generate a presigned URL for temporary access.
     */
    String generatePresignedUrl(String objectKey, int expiresInMinutes);

    /**
     * Download file content.
     */
    InputStream download(String objectKey);
}
