package com.example.filestorage.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Demo-only storage. It does not connect to AWS or create secure URLs. */
@Component
@Profile("local")
public class InMemoryObjectStorage implements ObjectStorage {
    private final List<StoredObject> objects = List.of(
            new StoredObject("sandy/archive/logistics-report.pdf", 204800, Instant.parse("2026-10-14T10:30:00Z")),
            new StoredObject("sandy/logistics-plan.docx", 48120, Instant.parse("2026-10-13T09:15:00Z")),
            new StoredObject("sandy/photos/holiday.jpg", 950000, Instant.parse("2026-10-12T08:00:00Z")),
            new StoredObject("sandy/empty-notes.txt", 0, Instant.parse("2026-10-11T08:00:00Z")),
            new StoredObject("sandy/archive/meeting-minutes.pdf", 78120, Instant.parse("2026-10-10T08:00:00Z")),
            new StoredObject("alex/logistics-invoice.xlsx", 61440, Instant.parse("2026-10-09T08:00:00Z")),
            new StoredObject("sandy/archive/", 0, Instant.parse("2026-10-08T08:00:00Z"))
    );
    @Override public List<StoredObject> listObjects(String prefix) {
        return objects.stream().filter(object -> object.key().startsWith(prefix)).toList();
    }
    @Override public String generateDownloadUrl(String objectKey, Duration expiry) {
        // Clearly simulated URL. This is not a real download URL or a security mechanism.
        return "http://localhost:8080/demo-download?key=" + URLEncoder.encode(objectKey, StandardCharsets.UTF_8)
                + "&expiresInSeconds=" + expiry.toSeconds();
    }
}
