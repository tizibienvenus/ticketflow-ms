package com.camergo.document.infrastructure.storage;

import com.camergo.document.application.service.StorageException;
import com.camergo.document.domain.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${storage.minio.bucket}")
    private String bucket;

    @Value("${storage.minio.endpoint}")
    private String endpoint;

    @Override
    public String upload(MultipartFile file, String objectKey) {
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = endpoint + "/" + bucket + "/" + objectKey;
            log.info("MinIO upload success: key={}", objectKey);
            return url;
        } catch (Exception e) {
            throw new StorageException("Failed to upload to MinIO: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            log.info("MinIO delete success: key={}", objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete from MinIO: " + objectKey, e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expiresInMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(expiresInMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to download from MinIO: " + objectKey, e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("MinIO bucket created: {}", bucket);
        }
    }
}
