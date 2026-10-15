package com.example.filestorage.controller;

import com.example.filestorage.dto.DownloadResponse;
import com.example.filestorage.dto.FileMetadataDto;
import com.example.filestorage.dto.FileSearchResponse;
import com.example.filestorage.model.PageResult;
import com.example.filestorage.service.FileService;
import com.example.filestorage.storage.StoredObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/users/{userName}/files")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService) { this.fileService = fileService; }

    @GetMapping
    @Operation(summary = "Search files by filename", description = "Search is case-insensitive and includes nested files.")
    public FileSearchResponse searchFiles(
            @PathVariable String userName,
            @RequestParam @NotBlank(message = "Search term must not be blank") String searchTerm,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        PageResult<StoredObject> result = fileService.searchFiles(userName, searchTerm, page, size);
        return new FileSearchResponse(result.items().stream()
                .map(item -> new FileMetadataDto(FileService.fileNameFromKey(item.key()), item.key(), item.sizeBytes(), item.lastModified()))
                .toList(), result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/download")
    @Operation(summary = "Create a temporary download URL", description = "Pass the full key returned by search; the URL expires after five minutes.")
    public DownloadResponse download(@PathVariable String userName,
            @RequestParam @NotBlank(message = "File key must not be blank") String key) {
        FileService.DownloadResult result = fileService.createDownload(userName, key);
        return new DownloadResponse(result.fileName(), result.downloadUrl(), result.expiresInSeconds());
    }
}
