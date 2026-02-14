package me.ifmo.backend.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    @Override
    public void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(props.getBucket()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(props.getBucket()).build()
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to ensure MinIO bucket: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void putObject(String objectKey, InputStream data, long sizeBytes, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .stream(data, sizeBytes, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException("MinIO putObject failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public InputStream getObject(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (ErrorResponseException exception) {
            throw new RuntimeException("MinIO getObject failed: " + exception.errorResponse().message(), exception);
        } catch (Exception exception) {
            throw new RuntimeException("MinIO getObject failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void removeObject(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception exception) {
            throw new RuntimeException("MinIO removeObject failed: " + exception.getMessage(), exception);
        }
    }
}
