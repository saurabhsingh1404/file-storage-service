package com.example.filestorage.service;

import com.example.filestorage.exception.FileNotFoundException;
import com.example.filestorage.exception.InvalidFileKeyException;
import com.example.filestorage.model.PageResult;
import com.example.filestorage.storage.ObjectStorage;
import com.example.filestorage.storage.StoredObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class FileService {
    private static final Pattern USER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private final ObjectStorage objectStorage;
    private final long downloadExpirySeconds;

    public FileService(ObjectStorage objectStorage,
                       @Value("${app.storage.download-url-expiry-seconds:300}") long downloadExpirySeconds) {
        this.objectStorage = objectStorage;
        this.downloadExpirySeconds = downloadExpirySeconds;
    }

    public PageResult<StoredObject> searchFiles(String userName, String searchTerm, int page, int size) {
        validateUserName(userName);
        if (searchTerm == null || searchTerm.isBlank()) throw new IllegalArgumentException("Search term must not be blank");
        if (page < 0) throw new IllegalArgumentException("Page must be zero or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");

        String prefix = userName + "/";
        String term = searchTerm.toLowerCase(Locale.ROOT);
        List<StoredObject> matches = objectStorage.listObjects(prefix).stream()
                .filter(object -> object.key() != null && object.key().startsWith(prefix))
                .filter(object -> !object.key().endsWith("/"))
                .filter(object -> fileNameFromKey(object.key()).toLowerCase(Locale.ROOT).contains(term))
                .sorted(Comparator.comparing(StoredObject::key))
                .toList();

        if (matches.isEmpty()) throw new FileNotFoundException("No files matched the search term");
        long startLong = (long) page * size;
        int start = (int) Math.min(startLong, matches.size());
        int end = (int) Math.min(startLong + size, matches.size());
        List<StoredObject> items = matches.subList(start, end);
        int totalPages = (int) ((matches.size() + (long) size - 1) / size);
        return new PageResult<>(items, page, size, matches.size(), totalPages);
    }

    public DownloadResult createDownload(String userName, String objectKey) {
        validateUserName(userName);
        if (objectKey == null || objectKey.isBlank()) throw new InvalidFileKeyException("File key must not be blank");
        String expectedPrefix = userName + "/";
        if (!objectKey.startsWith(expectedPrefix) || objectKey.endsWith("/")) {
            throw new InvalidFileKeyException("File key must belong to the requested user's prefix");
        }
        String url = objectStorage.generateDownloadUrl(objectKey, Duration.ofSeconds(downloadExpirySeconds));
        return new DownloadResult(fileNameFromKey(objectKey), url, downloadExpirySeconds);
    }

    private void validateUserName(String userName) {
        if (userName == null || !USER_NAME_PATTERN.matcher(userName).matches()) {
            throw new IllegalArgumentException("Username may contain only letters, numbers, dot, underscore, and hyphen");
        }
    }

    public static String fileNameFromKey(String key) {
        int slash = key.lastIndexOf('/');
        return key.substring(slash + 1);
    }

    public record DownloadResult(String fileName, String downloadUrl, long expiresInSeconds) { }
}
