package me.ifmo.backend.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    void ensureBucket();

    void putObject(String objectKey, InputStream data, long sizeBytes, String contentType);

    InputStream getObject(String objectKey);

    void removeObject(String objectKey);
}
