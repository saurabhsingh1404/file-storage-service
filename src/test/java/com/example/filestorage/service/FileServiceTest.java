package com.example.filestorage.service;

import com.example.filestorage.exception.FileNotFoundException;
import com.example.filestorage.exception.InvalidFileKeyException;
import com.example.filestorage.storage.ObjectStorage;
import com.example.filestorage.storage.StoredObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
    @Mock ObjectStorage storage;
    FileService service;
    @BeforeEach void setup() { service = new FileService(storage, 300); }

    @Test void searchesFilenameCaseInsensitivelyAndNested() {
        when(storage.listObjects("sandy/")).thenReturn(List.of(
                new StoredObject("sandy/archive/Logistics.PDF", 10, Instant.EPOCH),
                new StoredObject("sandy/logistics-plan.docx", 20, Instant.EPOCH),
                new StoredObject("sandy/logistics-folder/other.txt", 30, Instant.EPOCH)));
        var result = service.searchFiles("sandy", "LOGISTICS", 0, 50);
        assertEquals(2, result.totalElements());
        assertEquals("sandy/archive/Logistics.PDF", result.items().get(0).key());
    }

    @Test void noMatchesThrowsNotFound() {
        when(storage.listObjects("sandy/")).thenReturn(List.of(new StoredObject("sandy/a.txt", 1, Instant.EPOCH)));
        assertThrows(FileNotFoundException.class, () -> service.searchFiles("sandy", "missing", 0, 50));
    }

    @Test void outOfRangePageIsEmptyWhenMatchesExist() {
        when(storage.listObjects("sandy/")).thenReturn(List.of(new StoredObject("sandy/a.txt", 1, Instant.EPOCH)));
        var result = service.searchFiles("sandy", "a", 9, 1);
        assertTrue(result.items().isEmpty());
        assertEquals(1, result.totalElements());
    }

    @Test void rejectsAnotherUsersKey() {
        assertThrows(InvalidFileKeyException.class, () -> service.createDownload("sandy", "alex/private.txt"));
        verifyNoInteractions(storage);
    }

    @Test void retainsZeroByteFiles() {
        when(storage.listObjects("sandy/")).thenReturn(List.of(new StoredObject("sandy/empty.txt", 0, Instant.EPOCH)));
        assertEquals(1, service.searchFiles("sandy", "empty", 0, 50).totalElements());
    }
}
