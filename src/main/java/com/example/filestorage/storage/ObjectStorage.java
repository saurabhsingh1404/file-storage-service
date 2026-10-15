package com.example.filestorage.storage;

import java.time.Duration;
import java.util.List;

public interface ObjectStorage {
    List<StoredObject> listObjects(String prefix);
    String generateDownloadUrl(String objectKey, Duration expiry);
}
