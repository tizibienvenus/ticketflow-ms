package com.camergo.document.infrastructure.storage;

import com.camergo.document.application.service.StorageException;
import com.camergo.document.domain.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class S3StorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Value("${storage.s3.region}")
    private String region;

    @Override
    public String upload(MultipartFile file, String objectKey) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, objectKey);
            log.info("S3 upload success: key={}", objectKey);
            return url;
        } catch (Exception e) {
            throw new StorageException("Failed to upload to S3: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            log.info("S3 delete success: key={}", objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete from S3: " + objectKey, e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expiresInMinutes) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiresInMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build())
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate S3 presigned URL: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to download from S3: " + objectKey, e);
        }
    }
}
