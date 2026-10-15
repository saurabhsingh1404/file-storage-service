package com.example.filestorage.dto;

import java.util.List;

public record FileSearchResponse(List<FileMetadataDto> files, int page, int size, long totalElements, int totalPages) { }
