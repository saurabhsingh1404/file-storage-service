package com.example.filestorage.dto;

public record DownloadResponse(String fileName, String downloadUrl, long expiresInSeconds) { }
