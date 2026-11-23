package com.todoapp.storage;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataSync repository synchronization helper.
 */
class DataSyncTest {

    @Test
    @DisplayName("loadInto populates repositories and clears prior entries")
    void testLoadIntoPopulatesRepositories() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        // Populate repos initially with dummy entries
        taskRepo.save(new SimpleTask("Old Task", Priority.LOW, Category.DEFAULT_ID));
        categoryRepo.save(new Category(UUID.randomUUID(), "Old Category", "#000000"));

        Category work = new Category(UUID.randomUUID(), "Work", "#1976D2");
        SimpleTask newTask = new SimpleTask("New Task", Priority.HIGH, work.getId());

        AppData data = new AppData(List.of(Category.createDefault(), work), List.of(newTask));

        DataSync.loadInto(taskRepo, categoryRepo, data);

        assertEquals(2, categoryRepo.count());
        assertTrue(categoryRepo.findById(work.getId()).isPresent());

        assertEquals(1, taskRepo.count());
        assertEquals("New Task", taskRepo.findById(newTask.getId()).orElseThrow().getTitle());
    }

    @Test
    @DisplayName("snapshot captures consistent repository state")
    void testSnapshotCapturesCurrentState() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        Category study = new Category("Study", "#388E3C");
        categoryRepo.save(study);

        SimpleTask t1 = new SimpleTask("Task 1", Priority.MEDIUM, study.getId());
        taskRepo.save(t1);

        AppData snapshot = DataSync.snapshot(taskRepo, categoryRepo);

        assertEquals(AppData.CURRENT_SCHEMA_VERSION, snapshot.schemaVersion());
        assertEquals(2, snapshot.categories().size()); // default + study
        assertEquals(1, snapshot.tasks().size());
        assertEquals(t1.getId(), snapshot.tasks().get(0).getId());

        // Test overloaded method
        AppData snapshot2 = DataSync.snapshot(categoryRepo, taskRepo);
        assertEquals(snapshot.categories().size(), snapshot2.categories().size());
        assertEquals(snapshot.tasks().size(), snapshot2.tasks().size());
    }

    @Test
    @DisplayName("loadInto with null data is safe no-op")
    void testLoadIntoNullIsNoOp() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();
        taskRepo.save(new SimpleTask("Retained", Priority.LOW, Category.DEFAULT_ID));

        DataSync.loadInto(taskRepo, categoryRepo, null);

        assertEquals(1, taskRepo.count());
    }
}
