package com.todoapp.storage;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import com.todoapp.service.ChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for AutosaveListener verifying debounce behavior,
 * thread isolation, clean flushing on shutdown, and graceful error callback invocation.
 */
class AutosaveListenerTest {

    @Test
    @DisplayName("Autosave debounces multiple rapid mutations into a single disk write")
    void testAutosaveDebouncesRapidChanges() throws InterruptedException, StorageException {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        AtomicInteger saveCount = new AtomicInteger(0);
        Storage countingStorage = new Storage() {
            @Override
            public AppData load() {
                return AppData.empty();
            }

            @Override
            public void save(AppData data) {
                saveCount.incrementAndGet();
            }
        };

        // Use a short 50ms debounce for fast deterministic testing
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try (AutosaveListener listener = new AutosaveListener(
                countingStorage, taskRepo, categoryRepo, executor, null, 50L)) {

            // Fire 5 rapid events in < 20 ms
            for (int i = 0; i < 5; i++) {
                taskRepo.save(new SimpleTask("Task " + i, "", Priority.MEDIUM, Category.DEFAULT_ID));
                listener.onTasksChanged(ChangeType.CREATED, UUID.randomUUID());
                Thread.sleep(5);
            }

            // Immediately check: no save should have happened yet (still within 50ms window)
            assertEquals(0, saveCount.get(), "Save should not execute before debounce delay elapses");

            // Wait for debounce delay to expire
            Thread.sleep(120);

            // Exactly 1 save should have been committed for the batch
            assertEquals(1, saveCount.get(), "Multiple rapid events should be debounced into exactly 1 save");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("flush() immediately performs synchronous write")
    void testFlushExecutesSynchronously() throws StorageException {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        AtomicInteger saveCount = new AtomicInteger(0);
        Storage mockStorage = new Storage() {
            @Override
            public AppData load() {
                return AppData.empty();
            }

            @Override
            public void save(AppData data) {
                saveCount.incrementAndGet();
            }
        };

        try (AutosaveListener listener = new AutosaveListener(mockStorage, taskRepo, categoryRepo, msg -> {})) {
            taskRepo.save(new SimpleTask("Flush Me", "", Priority.HIGH, Category.DEFAULT_ID));
            listener.onTasksChanged(ChangeType.CREATED, UUID.randomUUID());

            // Flush synchronously
            listener.flush();

            assertEquals(1, saveCount.get(), "flush() must commit save immediately");
        }
    }

    @Test
    @DisplayName("close() flushes any pending writes before shutting down")
    void testCloseFlushesPendingSaves() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        AtomicInteger saveCount = new AtomicInteger(0);
        Storage mockStorage = new Storage() {
            @Override
            public AppData load() {
                return AppData.empty();
            }

            @Override
            public void save(AppData data) {
                saveCount.incrementAndGet();
            }
        };

        AutosaveListener listener = new AutosaveListener(mockStorage, taskRepo, categoryRepo, msg -> {});
        taskRepo.save(new SimpleTask("Closing Task", "", Priority.LOW, Category.DEFAULT_ID));
        listener.onTasksChanged(ChangeType.CREATED, UUID.randomUUID());

        // Close right away before debounce period completes
        listener.close();

        assertEquals(1, saveCount.get(), "close() must flush pending changes");
    }

    @Test
    @DisplayName("Storage exception invokes errorCallback without throwing or terminating caller")
    void testStorageErrorInvokesCallback() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        Storage failingStorage = new Storage() {
            @Override
            public AppData load() {
                return AppData.empty();
            }

            @Override
            public void save(AppData data) throws StorageException {
                throw new StorageException("Disk full / permission denied");
            }
        };

        List<String> errors = new ArrayList<>();
        try (AutosaveListener listener = new AutosaveListener(failingStorage, taskRepo, categoryRepo, errors::add)) {
            assertDoesNotThrow(() -> {
                listener.onTasksChanged(ChangeType.CREATED, UUID.randomUUID());
                listener.flush();
            });

            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("Disk full / permission denied"));
        }
    }

    @Test
    @DisplayName("Convenience constructors and closed state guard")
    void testConvenienceConstructorsAndClosedGuard() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();
        Storage dummyStorage = new Storage() {
            @Override
            public AppData load() { return AppData.empty(); }
            @Override
            public void save(AppData data) {}
        };

        // Test constructor 1 (storage, taskRepo, categoryRepo)
        try (AutosaveListener listener = new AutosaveListener(dummyStorage, taskRepo, categoryRepo)) {
            assertNotNull(listener);
        }

        // Test constructor 2 (storage, taskRepo, categoryRepo, executor)
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try (AutosaveListener listener2 = new AutosaveListener(dummyStorage, taskRepo, categoryRepo, executor)) {
            assertNotNull(listener2);
            // Calling close() twice is safe
            listener2.close();
            // onTasksChanged after close is a no-op
            assertDoesNotThrow(() -> listener2.onTasksChanged(ChangeType.UPDATED, UUID.randomUUID()));
        } finally {
            executor.shutdownNow();
        }
    }
}
