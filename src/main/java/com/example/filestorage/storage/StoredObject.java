package com.example.filestorage.storage;

import java.time.Instant;

public record StoredObject(String key, long sizeBytes, Instant lastModified) { }
