package com.todoapp;

import com.todoapp.model.Category;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskType;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import com.todoapp.service.CategoryService;
import com.todoapp.service.NotFoundException;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskQuery;
import com.todoapp.service.TaskService;
import com.todoapp.service.ValidationException;
import com.todoapp.storage.AppData;
import com.todoapp.storage.JsonStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case and integration tests for M12 hardening.
 * Covers boundary inputs, Unicode, timing, undo, category cascades,
 * and a full JsonStorage round-trip.
 */
class EdgeCaseAndIntegrationTest {

    private InMemoryTaskRepository taskRepo;
    private InMemoryCategoryRepository categoryRepo;
    private TaskService taskService;
    private CategoryService categoryService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        taskRepo = new InMemoryTaskRepository();
        categoryRepo = new InMemoryCategoryRepository();
        fixedClock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("UTC"));
        taskService = new TaskService(taskRepo, categoryRepo, fixedClock);
        categoryService = new CategoryService(categoryRepo, taskRepo);
    }

    // =========================================================================
    // 1. Title boundary tests
    // =========================================================================

    @Test
    @DisplayName("Title at exactly MAX_TITLE_LENGTH (100 chars) is accepted")
    void testTitleAtMaxLength() {
        String maxTitle = "A".repeat(100);
        TaskDraft draft = TaskDraft.ofSimple(maxTitle, Priority.LOW, Category.DEFAULT_ID);
        Task task = taskService.create(draft);
        assertEquals(maxTitle, task.getTitle());
    }

    @Test
    @DisplayName("Title exceeding MAX_TITLE_LENGTH (101 chars) is rejected with ValidationException")
    void testTitleExceedsMaxLength() {
        String longTitle = "B".repeat(101);
        TaskDraft draft = TaskDraft.ofSimple(longTitle, Priority.LOW, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.create(draft));
    }

    @Test
    @DisplayName("Title with only whitespace is rejected with ValidationException")
    void testWhitespaceTitleRejected() {
        TaskDraft draft = TaskDraft.ofSimple("   ", Priority.MEDIUM, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.create(draft));
    }

    @Test
    @DisplayName("Title with null is rejected with ValidationException")
    void testNullTitleRejected() {
        TaskDraft draft = TaskDraft.ofSimple(null, Priority.MEDIUM, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.create(draft));
    }

    // =========================================================================
    // 2. Unicode / emoji titles
    // =========================================================================

    @Test
    @DisplayName("Unicode emoji and CJK characters in title are stored and retrieved correctly")
    void testUnicodeEmojiTitle() {
        String unicodeTitle = "\uD83D\uDE80 Deploy \u4e2d\u6587";
        TaskDraft draft = TaskDraft.ofSimple(unicodeTitle, Priority.HIGH, Category.DEFAULT_ID);
        Task created = taskService.create(draft);
        assertEquals(unicodeTitle, created.getTitle());

        Task fetched = taskService.get(created.getId());
        assertEquals(unicodeTitle, fetched.getTitle());
    }

    // =========================================================================
    // 3. Deadline overdue logic
    // =========================================================================

    @Test
    @DisplayName("Deadline task due yesterday is overdue")
    void testDeadlinePastIsOverdue() {
        LocalDate today = LocalDate.now(fixedClock);
        LocalDate yesterday = today.minusDays(1);
        TaskDraft draft = TaskDraft.ofDeadline("Past task", Priority.HIGH, Category.DEFAULT_ID, yesterday);
        Task task = taskService.create(draft);

        DeadlineTask dt = (DeadlineTask) taskService.get(task.getId());
        assertTrue(dt.isOverdue(today), "Past-due task should be overdue");
    }

    @Test
    @DisplayName("Deadline task due today is NOT overdue (boundary: isBefore fails)")
    void testDeadlineTodayNotOverdue() {
        LocalDate today = LocalDate.now(fixedClock);
        TaskDraft draft = TaskDraft.ofDeadline("Today task", Priority.MEDIUM, Category.DEFAULT_ID, today);
        Task task = taskService.create(draft);

        DeadlineTask dt = (DeadlineTask) taskService.get(task.getId());
        assertFalse(dt.isOverdue(today), "Task due today should NOT be overdue");
    }

    @Test
    @DisplayName("Deadline task due far in the future is not overdue")
    void testDeadlineFutureNotOverdue() {
        LocalDate today = LocalDate.now(fixedClock);
        LocalDate farFuture = today.plusYears(5);
        TaskDraft draft = TaskDraft.ofDeadline("Future task", Priority.LOW, Category.DEFAULT_ID, farFuture);
        Task task = taskService.create(draft);

        DeadlineTask dt = (DeadlineTask) taskService.get(task.getId());
        assertFalse(dt.isOverdue(today), "Far-future task should not be overdue");
    }

    @Test
    @DisplayName("Completed deadline task is never overdue regardless of date")
    void testCompletedDeadlineNotOverdue() {
        LocalDate today = LocalDate.now(fixedClock);
        LocalDate yesterday = today.minusDays(1);
        TaskDraft draft = TaskDraft.ofDeadline("Done task", Priority.HIGH, Category.DEFAULT_ID, yesterday);
        Task task = taskService.create(draft);
        taskService.toggleComplete(task.getId());

        DeadlineTask dt = (DeadlineTask) taskService.get(task.getId());
        assertFalse(dt.isOverdue(today), "Completed task must not be overdue");
    }

    // =========================================================================
    // 4. Clock injection — deterministic timestamps
    // =========================================================================

    @Test
    @DisplayName("Task createdAt/updatedAt match the injected fixed clock")
    void testFixedClockTimestamps() {
        Instant fixedInstant = Instant.parse("2026-06-15T12:00:00Z");
        Clock c = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
        TaskService ts = new TaskService(taskRepo, categoryRepo, c);

        TaskDraft draft = TaskDraft.ofSimple("Clock test", Priority.MEDIUM, Category.DEFAULT_ID);
        Task task = ts.create(draft);

        assertEquals(
                java.time.LocalDateTime.ofInstant(fixedInstant, ZoneId.of("UTC")),
                task.getCreatedAt()
        );
        assertEquals(
                java.time.LocalDateTime.ofInstant(fixedInstant, ZoneId.of("UTC")),
                task.getUpdatedAt()
        );
    }

    // =========================================================================
    // 5. Toggle complete idempotency
    // =========================================================================

    @Test
    @DisplayName("Toggling complete twice returns task to original incomplete state")
    void testToggleCompleteTwiceRestoresState() {
        TaskDraft draft = TaskDraft.ofSimple("Toggle test", Priority.MEDIUM, Category.DEFAULT_ID);
        Task task = taskService.create(draft);

        assertFalse(task.isCompleted(), "Should start incomplete");
        taskService.toggleComplete(task.getId());
        assertTrue(taskService.get(task.getId()).isCompleted(), "Should be completed after first toggle");
        taskService.toggleComplete(task.getId());
        assertFalse(taskService.get(task.getId()).isCompleted(), "Should be incomplete after second toggle");
    }

    // =========================================================================
    // 6. Undo / canUndo
    // =========================================================================

    @Test
    @DisplayName("canUndo is false before any delete, true after delete, false after undoDelete")
    void testCanUndoLifecycle() {
        TaskDraft draft = TaskDraft.ofSimple("Undo me", Priority.MEDIUM, Category.DEFAULT_ID);
        Task task = taskService.create(draft);

        assertFalse(taskService.canUndo(), "No undo buffer initially");
        taskService.delete(task.getId());
        assertTrue(taskService.canUndo(), "Undo buffer populated after delete");

        Optional<Task> restored = taskService.undoDelete();
        assertTrue(restored.isPresent(), "undoDelete should return restored task");
        assertFalse(taskService.canUndo(), "Undo buffer cleared after restore");
    }

    @Test
    @DisplayName("undoDelete on empty buffer returns empty Optional")
    void testUndoDeleteEmptyBuffer() {
        Optional<Task> result = taskService.undoDelete();
        assertTrue(result.isEmpty(), "undoDelete with empty buffer returns empty");
    }

    @Test
    @DisplayName("Second delete overwrites undo buffer — only last deleted task is restorable")
    void testUndoBufferSingleSlot() {
        Task t1 = taskService.create(TaskDraft.ofSimple("First", Priority.LOW, Category.DEFAULT_ID));
        Task t2 = taskService.create(TaskDraft.ofSimple("Second", Priority.HIGH, Category.DEFAULT_ID));

        taskService.delete(t1.getId());
        taskService.delete(t2.getId());

        Optional<Task> restored = taskService.undoDelete();
        assertTrue(restored.isPresent());
        assertEquals("Second", restored.get().getTitle(), "Should restore most recently deleted task");
    }

    // =========================================================================
    // 7. Category deletion cascade
    // =========================================================================

    @Test
    @DisplayName("Tasks assigned to deleted category are reassigned to General")
    void testDeleteCategoryReassignsTasks() {
        Category cat = categoryService.create("Work", "#FF0000");
        Task task = taskService.create(TaskDraft.ofSimple("Work item", Priority.MEDIUM, cat.getId()));

        assertEquals(cat.getId(), taskService.get(task.getId()).getCategoryId(),
                "Task should be in Work category before deletion");

        categoryService.delete(cat.getId());

        Task after = taskService.get(task.getId());
        assertEquals(Category.DEFAULT_ID, after.getCategoryId(),
                "Task should be reassigned to General after category deleted");
    }

    @Test
    @DisplayName("Cannot delete the default General category")
    void testCannotDeleteDefaultCategory() {
        assertThrows(ValidationException.class, () -> categoryService.delete(Category.DEFAULT_ID));
    }

    @Test
    @DisplayName("Cannot rename the default General category")
    void testCannotRenameDefaultCategory() {
        assertThrows(ValidationException.class, () -> categoryService.rename(Category.DEFAULT_ID, "Renamed"));
    }

    // =========================================================================
    // 8. Category filter — delete while filtered
    // =========================================================================

    @Test
    @DisplayName("Deleting a task while a category filter is active correctly reduces count")
    void testDeleteWhileCategoryFiltered() {
        Category cat = categoryService.create("Projects", "#00FF00");
        Task t1 = taskService.create(TaskDraft.ofSimple("Task A", Priority.MEDIUM, cat.getId()));
        Task t2 = taskService.create(TaskDraft.ofSimple("Task B", Priority.MEDIUM, cat.getId()));
        taskService.create(TaskDraft.ofSimple("Task C", Priority.MEDIUM, Category.DEFAULT_ID));

        TaskQuery filteredQuery = TaskQuery.defaultQuery().withCategory(cat.getId());
        assertEquals(2, taskService.query(filteredQuery).size(), "Should show 2 tasks in Projects before deletion");

        taskService.delete(t1.getId());

        assertEquals(1, taskService.query(filteredQuery).size(), "Should show 1 task after deletion under filter");
        assertEquals("Task B", taskService.query(filteredQuery).get(0).getTitle());
    }

    // =========================================================================
    // 9. Category validation
    // =========================================================================

    @Test
    @DisplayName("Creating a task with a non-existent categoryId throws ValidationException")
    void testTaskWithInvalidCategoryRejected() {
        UUID nonExistent = UUID.randomUUID();
        TaskDraft draft = TaskDraft.ofSimple("Ghost task", Priority.MEDIUM, nonExistent);
        assertThrows(ValidationException.class, () -> taskService.create(draft));
    }

    @Test
    @DisplayName("Duplicate category name (case-insensitive) is rejected")
    void testDuplicateCategoryNameRejected() {
        categoryService.create("Alpha", "#123456");
        assertThrows(ValidationException.class, () -> categoryService.create("alpha", "#654321"));
        assertThrows(ValidationException.class, () -> categoryService.create("ALPHA", "#111111"));
        assertThrows(ValidationException.class, () -> categoryService.create("  Alpha  ", "#222222"));
    }

    @Test
    @DisplayName("Invalid hex color format is rejected with ValidationException")
    void testInvalidHexColorRejected() {
        assertThrows(ValidationException.class, () -> categoryService.create("Cat1", "not-a-color"));
        assertThrows(ValidationException.class, () -> categoryService.create("Cat2", "#12345"));
        assertThrows(ValidationException.class, () -> categoryService.create("Cat3", "#1234567"));
        assertThrows(ValidationException.class, () -> categoryService.create("Cat4", "123456"));
        assertThrows(ValidationException.class, () -> categoryService.create("Cat5", null));
    }

    // =========================================================================
    // 10. Query engine edge cases
    // =========================================================================

    @Test
    @DisplayName("Query with multiple filters: active tasks with priority filter")
    void testActiveAndPriorityFilter() {
        Task active = taskService.create(TaskDraft.ofSimple("Active", Priority.MEDIUM, Category.DEFAULT_ID));
        Task done = taskService.create(TaskDraft.ofSimple("Done", Priority.MEDIUM, Category.DEFAULT_ID));
        taskService.toggleComplete(done.getId());

        TaskQuery q = TaskQuery.defaultQuery()
                .withStatus(StatusFilter.ACTIVE)
                .withPriority(Priority.MEDIUM);

        List<Task> results = taskService.query(q);
        assertEquals(1, results.size());
        assertEquals(active.getId(), results.get(0).getId());
    }

    @Test
    @DisplayName("Completed status filter only returns completed tasks")
    void testCompletedStatusFilter() {
        taskService.create(TaskDraft.ofSimple("Pending", Priority.MEDIUM, Category.DEFAULT_ID));
        Task done = taskService.create(TaskDraft.ofSimple("Finished", Priority.LOW, Category.DEFAULT_ID));
        taskService.toggleComplete(done.getId());

        TaskQuery q = TaskQuery.defaultQuery().withStatus(StatusFilter.COMPLETED);
        List<Task> results = taskService.query(q);
        assertEquals(1, results.size());
        assertEquals(done.getId(), results.get(0).getId());
    }

    @Test
    @DisplayName("Search query matches case-insensitively")
    void testSearchCaseInsensitive() {
        taskService.create(TaskDraft.ofSimple("Buy Milk", Priority.LOW, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Read Book", Priority.LOW, Category.DEFAULT_ID));

        List<Task> milk = taskService.query(TaskQuery.defaultQuery().withSearch("MILK"));
        assertEquals(1, milk.size());
        assertEquals("Buy Milk", milk.get(0).getTitle());
    }

    @Test
    @DisplayName("Sorting by priority: HIGH precedes LOW in ascending order")
    void testSortByPriorityAscending() {
        taskService.create(TaskDraft.ofSimple("High prio", Priority.HIGH, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Low prio", Priority.LOW, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Med prio", Priority.MEDIUM, Category.DEFAULT_ID));

        List<Task> sorted = taskService.query(
                TaskQuery.defaultQuery().withSort(com.todoapp.service.SortOption.PRIORITY, true));

        assertEquals(Priority.HIGH, sorted.get(0).getPriority());
        assertEquals(Priority.MEDIUM, sorted.get(1).getPriority());
        assertEquals(Priority.LOW, sorted.get(2).getPriority());
    }

    @Test
    @DisplayName("Pinned task always appears first regardless of sort direction")
    void testPinnedTaskAlwaysFirst() {
        Task normal = taskService.create(TaskDraft.ofSimple("Normal", Priority.LOW, Category.DEFAULT_ID));
        Task pinned = taskService.create(TaskDraft.ofSimple("Pinned", Priority.LOW, Category.DEFAULT_ID));
        taskService.togglePinned(pinned.getId());

        List<Task> results = taskService.query(
                TaskQuery.defaultQuery().withSort(com.todoapp.service.SortOption.TITLE, true));

        assertEquals(pinned.getId(), results.get(0).getId(), "Pinned task must be first in list");
    }

    @Test
    @DisplayName("Toggling pinned state updates pinned flag correctly")
    void testTogglePinned() {
        Task task = taskService.create(TaskDraft.ofSimple("Pin toggle", Priority.MEDIUM, Category.DEFAULT_ID));
        assertFalse(task.isPinned());

        taskService.togglePinned(task.getId());
        assertTrue(taskService.get(task.getId()).isPinned());

        taskService.togglePinned(task.getId());
        assertFalse(taskService.get(task.getId()).isPinned());
    }

    // =========================================================================
    // 11. Integration test: JsonStorage round-trip
    // =========================================================================

    @Test
    @DisplayName("Integration: save and reload task types via JsonStorage preserves all fields")
    void testJsonStorageRoundTrip(@TempDir Path tempDir) throws Exception {
        // Build a full object graph via services
        Category workCat = categoryService.create("Work", "#1A2B3C");

        Task simple = taskService.create(TaskDraft.ofSimple(
                "Simple task", Priority.HIGH, workCat.getId()));

        LocalDate dueDate = LocalDate.of(2027, 3, 15);
        Task deadline = taskService.create(TaskDraft.ofDeadline(
                "Deadline task", Priority.LOW, workCat.getId(), dueDate));

        // Complete the simple task and pin the deadline task
        taskService.toggleComplete(simple.getId());
        taskService.togglePinned(deadline.getId());

        // Build AppData snapshot from live repos
        List<com.todoapp.model.Category> categories = categoryRepo.findAll();
        List<Task> tasks = taskRepo.findAll();
        AppData saved = new AppData(categories, tasks);

        // Save to temp file and reload into fresh object graph
        Path dataFile = tempDir.resolve("todo-test.json");
        JsonStorage storage = new JsonStorage(dataFile);
        storage.save(saved);

        assertTrue(Files.exists(dataFile), "Data file should exist after save");
        assertTrue(Files.size(dataFile) > 0, "Data file should not be empty");

        AppData loaded = storage.load();
        assertFalse(storage.getLastLoadWarning().isPresent(), "No warnings should be generated for clean data");

        // Verify category count
        assertEquals(categories.size(), loaded.categories().size(),
                "Loaded category count should match saved count");

        // Find and verify Work category
        Optional<com.todoapp.model.Category> loadedWork = loaded.categories().stream()
                .filter(c -> "Work".equals(c.getName()))
                .findFirst();
        assertTrue(loadedWork.isPresent(), "Work category should be present after load");
        assertEquals("#1A2B3C", loadedWork.get().getColorHex());

        // Verify task count
        assertEquals(2, loaded.tasks().size(), "Should have 2 tasks after round-trip");

        // Verify simple task attributes
        Optional<Task> loadedSimple = loaded.tasks().stream()
                .filter(t -> t.getId().equals(simple.getId()))
                .findFirst();
        assertTrue(loadedSimple.isPresent(), "Simple task must survive round-trip");
        assertEquals("Simple task", loadedSimple.get().getTitle());
        assertEquals(Priority.HIGH, loadedSimple.get().getPriority());
        assertTrue(loadedSimple.get().isCompleted(), "Completed flag should be preserved");
        assertEquals(workCat.getId(), loadedSimple.get().getCategoryId());

        // Verify deadline task
        Optional<Task> loadedDeadline = loaded.tasks().stream()
                .filter(t -> t.getId().equals(deadline.getId()))
                .findFirst();
        assertTrue(loadedDeadline.isPresent(), "Deadline task must survive round-trip");
        DeadlineTask dt = (DeadlineTask) loadedDeadline.get();
        assertEquals(dueDate, dt.getDueDateValue(), "Due date should be preserved exactly");
        assertTrue(dt.isPinned(), "Pinned flag should be preserved");
        assertEquals(Priority.LOW, dt.getPriority());
    }

    @Test
    @DisplayName("Integration: loading from non-existent file returns empty AppData with General category")
    void testLoadNonExistentFileReturnsEmpty(@TempDir Path tempDir) throws Exception {
        Path dataFile = tempDir.resolve("nonexistent.json");
        JsonStorage storage = new JsonStorage(dataFile);
        AppData data = storage.load();

        assertFalse(storage.getLastLoadWarning().isPresent());
        assertEquals(1, data.categories().size(), "Should have exactly the General category");
        assertEquals(Category.DEFAULT_ID, data.categories().get(0).getId());
        assertTrue(data.tasks().isEmpty());
    }

    @Test
    @DisplayName("Integration: corrupt file is quarantined and empty AppData returned")
    void testCorruptFileIsQuarantined(@TempDir Path tempDir) throws Exception {
        Path dataFile = tempDir.resolve("corrupt.json");
        Files.writeString(dataFile, "{ this is not valid json !!!! }");

        JsonStorage storage = new JsonStorage(dataFile);
        AppData data = storage.load();

        // Corrupt file should be quarantined (renamed), not remain as the data file
        assertFalse(Files.exists(dataFile), "Original corrupt file should be quarantined (renamed/moved)");
        assertTrue(storage.getLastLoadWarning().isPresent(), "Warning should be set for corrupt file");
        assertTrue(storage.getLastLoadWarning().get().contains("quarantined"),
                "Warning message should mention quarantine");
        assertTrue(data.tasks().isEmpty(), "Fallback data should be empty");
    }

    // =========================================================================
    // 12. Additional Stress and Edge Cases
    // =========================================================================

    @Test
    @DisplayName("Performance: 1,000+ tasks can be created, queried and filtered rapidly")
    void testPerformanceWith1000Tasks() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            taskRepo.save(new com.todoapp.model.SimpleTask("Task #" + i, Priority.MEDIUM, Category.DEFAULT_ID));
        }
        assertEquals(1000, taskRepo.count());

        List<Task> results = taskService.query(TaskQuery.defaultQuery());
        assertEquals(1000, results.size());

        List<Task> filtered = taskService.query(TaskQuery.defaultQuery().withSearch("Task #500"));
        assertEquals(1, filtered.size());

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 2000, "1000 tasks creation and queries should complete well within 2 seconds, took: " + duration + "ms");
    }

    @Test
    @DisplayName("Edge Case: Editing a task whose category was deleted reassigns to General smoothly")
    void testEditTaskWhoseCategoryWasDeleted() {
        com.todoapp.model.Category cat = categoryService.create("Temporary Cat", "#FF5722");
        Task task = taskService.create(TaskDraft.ofSimple("Cat Task", Priority.HIGH, cat.getId()));
        assertEquals(cat.getId(), task.getCategoryId());

        // Delete the category
        categoryService.delete(cat.getId());

        // Task should now belong to General
        Task reloaded = taskService.get(task.getId());
        assertEquals(Category.DEFAULT_ID, reloaded.getCategoryId());

        // Editing the task works cleanly without error
        TaskDraft updateDraft = TaskDraft.ofSimple("Updated Title", Priority.LOW, Category.DEFAULT_ID);
        Task updated = taskService.update(task.getId(), updateDraft);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(Category.DEFAULT_ID, updated.getCategoryId());
    }

    @Test
    @DisplayName("Edge Case: Rapid repeated complete toggles execute safely and deterministically")
    void testRapidRepeatedToggles() {
        Task task = taskService.create(TaskDraft.ofSimple("Toggle Task", Priority.LOW, Category.DEFAULT_ID));
        assertFalse(task.isCompleted());

        for (int i = 0; i < 50; i++) {
            taskService.toggleComplete(task.getId());
        }
        // Even number of toggles leaves task in its original false state
        assertFalse(taskService.get(task.getId()).isCompleted());

        taskService.toggleComplete(task.getId());
        assertTrue(taskService.get(task.getId()).isCompleted());
    }

    @Test
    @DisplayName("Edge Case: Saving to unwritable location throws StorageException without crashing")
    void testUnwritableDataFileThrowsStorageException(@TempDir Path tempDir) {
        // Path pointing to an existing directory will fail atomic rename/write
        Path invalidPath = tempDir.resolve("some_dir");
        try {
            Files.createDirectory(invalidPath);
        } catch (IOException e) {
            fail("Failed to create test directory");
        }
        JsonStorage storage = new JsonStorage(invalidPath);
        assertThrows(com.todoapp.storage.StorageException.class, () -> storage.save(AppData.empty()));
    }
}
