package com.todoapp.storage;

import com.todoapp.model.Category;
import com.todoapp.model.ChecklistItem;
import com.todoapp.model.ChecklistTask;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and robustness tests for JsonStorage covering atomic saves,
 * corruption quarantine, missing file handling, and multi-instance round-trips.
 */
class JsonStorageTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Full round-trip between two separate JsonStorage instances")
    void testRoundTripBetweenInstances() throws StorageException {
        Path dataFile = tempDir.resolve("data.json");
        JsonStorage storage1 = new JsonStorage(dataFile);

        Category work = new Category("Work", "#1E88E5");
        SimpleTask task1 = new SimpleTask("Task 1", "Desc 1", Priority.HIGH, work.getId());
        ChecklistTask task2 = new ChecklistTask("Task 2", "Desc 2", Priority.MEDIUM, Category.DEFAULT_ID,
                List.of(new ChecklistItem("Sub 1"), new ChecklistItem("Sub 2")));
        DeadlineTask task3 = new DeadlineTask("Task 3", "Desc 3", Priority.LOW, work.getId(), LocalDate.of(2026, 12, 1));

        AppData toSave = new AppData(List.of(Category.createDefault(), work), List.of(task1, task2, task3));
        storage1.save(toSave);

        assertTrue(Files.exists(dataFile));

        // Read back with completely independent storage instance
        JsonStorage storage2 = new JsonStorage(dataFile);
        AppData loaded = storage2.load();

        assertEquals(2, loaded.categories().size());
        assertEquals(3, loaded.tasks().size());
        assertEquals(task1.getTitle(), loaded.tasks().get(0).getTitle());
        assertEquals(task2.getTitle(), loaded.tasks().get(1).getTitle());
        assertEquals(task3.getTitle(), loaded.tasks().get(2).getTitle());
        assertTrue(storage2.getLastLoadWarning().isEmpty());
    }

    @Test
    @DisplayName("Missing file returns AppData.empty() with default category")
    void testMissingFileReturnsDefaultData() throws StorageException {
        Path missing = tempDir.resolve("nonexistent.json");
        JsonStorage storage = new JsonStorage(missing);

        AppData data = storage.load();

        assertNotNull(data);
        assertEquals(1, data.categories().size());
        assertEquals(Category.DEFAULT_ID, data.categories().get(0).getId());
        assertEquals("General", data.categories().get(0).getName());
        assertTrue(data.tasks().isEmpty());
        assertTrue(storage.getLastLoadWarning().isEmpty());
    }

    @Test
    @DisplayName("Empty (0 bytes) file returns AppData.empty() cleanly")
    void testEmptyFileReturnsDefaultData() throws IOException, StorageException {
        Path emptyFile = tempDir.resolve("empty.json");
        Files.writeString(emptyFile, "");

        JsonStorage storage = new JsonStorage(emptyFile);
        AppData data = storage.load();

        assertNotNull(data);
        assertEquals(1, data.categories().size());
        assertTrue(data.tasks().isEmpty());
    }

    @Test
    @DisplayName("Corrupt / truncated JSON causes file quarantine and sets warning")
    void testCorruptedFileIsQuarantined() throws IOException, StorageException {
        Path corruptFile = tempDir.resolve("data.json");
        Files.writeString(corruptFile, "{ \"schemaVersion\": 1, \"tasks\": [ { broken json");

        JsonStorage storage = new JsonStorage(corruptFile);
        AppData data = storage.load();

        assertNotNull(data);
        assertEquals(1, data.categories().size());
        assertTrue(data.tasks().isEmpty());

        // The original corrupted file should no longer exist at original path
        assertFalse(Files.exists(corruptFile), "Original corrupt file must be moved/renamed");

        // Quarantined file should exist in the directory
        boolean hasQuarantineFile;
        try (var stream = Files.list(tempDir)) {
            hasQuarantineFile = stream.anyMatch(p -> p.getFileName().toString().contains("data.json.corrupt-"));
        }
        assertTrue(hasQuarantineFile, "Quarantined backup file must exist in directory");

        // Warning message must be present
        Optional<String> warning = storage.getLastLoadWarning();
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("corrupted and quarantined"));
    }

    @Test
    @DisplayName("Atomic save leaves no .tmp files on disk")
    void testAtomicSaveLeavesNoTempFile() throws StorageException {
        Path targetFile = tempDir.resolve("subdir/tasks.json");
        JsonStorage storage = new JsonStorage(targetFile);

        AppData data = AppData.empty();
        storage.save(data);

        assertTrue(Files.exists(targetFile));
        Path tmpFile = targetFile.resolveSibling("tasks.json.tmp");
        assertFalse(Files.exists(tmpFile), "Temporary file must not remain after successful atomic save");
    }

    @Test
    @DisplayName("Unknown task type in file is skipped and warning is populated in getLastLoadWarning")
    void testUnknownTaskTypePopulatesWarning() throws IOException, StorageException {
        Path file = tempDir.resolve("tasks_with_unknown.json");
        String json = """
                {
                  "schemaVersion": 1,
                  "categories": [],
                  "tasks": [
                    {
                      "type": "FUTURE_SPECIAL_TYPE",
                      "id": "11111111-1111-1111-1111-111111111111",
                      "title": "Future"
                    }
                  ]
                }
                """;
        Files.writeString(file, json);

        JsonStorage storage = new JsonStorage(file);
        AppData data = storage.load();

        assertTrue(data.tasks().isEmpty());
        assertTrue(storage.getLastLoadWarning().isPresent());
        assertTrue(storage.getLastLoadWarning().get().contains("FUTURE_SPECIAL_TYPE"));
    }

    @Test
    @DisplayName("StorageException constructors and getFilePath")
    void testStorageExceptionAndGetFilePath() {
        Path path = tempDir.resolve("some/path.json");
        JsonStorage storage = new JsonStorage(path);
        assertEquals(path, storage.getFilePath());

        StorageException ex1 = new StorageException("error message");
        assertEquals("error message", ex1.getMessage());

        StorageException ex2 = new StorageException("cause message", new RuntimeException("root cause"));
        assertEquals("cause message", ex2.getMessage());
        assertNotNull(ex2.getCause());
    }

    @Test
    @DisplayName("save throws NullPointerException when data is null")
    void testSaveNullThrows() {
        JsonStorage storage = new JsonStorage(tempDir.resolve("null_test.json"));
        assertThrows(NullPointerException.class, () -> storage.save(null));
    }
}
