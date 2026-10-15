package com.example.filestorage.dto;

import java.time.Instant;

public record FileMetadataDto(String fileName, String key, long sizeBytes, Instant lastModified) { }
