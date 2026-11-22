package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.ChecklistTask;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskType;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TaskService covering CRUD, validations,
 * copy isolation, undo buffers, state toggles, and observer notifications.
 */
class TaskServiceTest {

    private InMemoryTaskRepository taskRepo;
    private InMemoryCategoryRepository categoryRepo;
    private Clock fixedClock;
    private TaskService taskService;
    private List<String> eventLog;

    @BeforeEach
    void setUp() {
        taskRepo = new InMemoryTaskRepository();
        categoryRepo = new InMemoryCategoryRepository();
        fixedClock = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), ZoneId.of("UTC"));
        taskService = new TaskService(taskRepo, categoryRepo, fixedClock);
        eventLog = new ArrayList<>();

        taskService.addListener((type, id) -> eventLog.add(type.name() + ":" + id));
    }

    // =========================================================================
    // 1. Create Task Tests
    // =========================================================================

    @Test
    @DisplayName("Create task of each type: success, timestamps and listener notifications")
    void testCreateTaskSuccess() {
        // Simple task
        TaskDraft simpleDraft = TaskDraft.ofSimple("Buy groceries", "Milk and eggs", Priority.HIGH, Category.DEFAULT_ID);
        Task simpleTask = taskService.create(simpleDraft);

        assertInstanceOf(SimpleTask.class, simpleTask);
        assertEquals("Buy groceries", simpleTask.getTitle());
        assertEquals(Priority.HIGH, simpleTask.getPriority());
        assertEquals(1, eventLog.size());
        assertTrue(eventLog.get(0).startsWith("CREATED:" + simpleTask.getId()));

        // Checklist task
        TaskDraft checklistDraft = TaskDraft.ofChecklist("Travel prep", "Packing", Priority.MEDIUM, Category.DEFAULT_ID, List.of("Passport", "Tickets"));
        Task checklistTask = taskService.create(checklistDraft);
        assertInstanceOf(ChecklistTask.class, checklistTask);
        assertEquals(2, ((ChecklistTask) checklistTask).getItems().size());

        // Deadline task
        TaskDraft deadlineDraft = TaskDraft.ofDeadline("Tax filing", "Submit returns", Priority.LOW, Category.DEFAULT_ID, LocalDate.of(2026, 12, 15));
        Task deadlineTask = taskService.create(deadlineDraft);
        assertInstanceOf(DeadlineTask.class, deadlineTask);
        assertEquals(LocalDate.of(2026, 12, 15), ((DeadlineTask) deadlineTask).getDueDateValue());

        assertEquals(3, taskRepo.count());
        assertEquals(3, eventLog.size());
    }

    @Test
    @DisplayName("Create task: copy isolation ensures modifying returned entity does not affect repository")
    void testCreateCopyIsolation() {
        TaskDraft draft = TaskDraft.ofSimple("Original Title", "Original Desc", Priority.MEDIUM, Category.DEFAULT_ID);
        Task returnedTask = taskService.create(draft);

        // Mutate the returned task instance
        returnedTask.setTitle("Mutated Title Outside");
        returnedTask.setPriority(Priority.LOW);

        // Fetch stored task from repository and verify it was NOT mutated
        Task storedTask = taskRepo.findById(returnedTask.getId()).orElseThrow();
        assertEquals("Original Title", storedTask.getTitle());
        assertEquals(Priority.MEDIUM, storedTask.getPriority());
    }

    @Test
    @DisplayName("Create task: validation failures do not fire change events")
    void testCreateValidationFailures() {
        assertThrows(ValidationException.class, () -> taskService.create(null));

        TaskDraft blankTitle = TaskDraft.ofSimple("", "Desc", Priority.MEDIUM, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.create(blankTitle));

        TaskDraft overTitle = TaskDraft.ofSimple("x".repeat(101), "Desc", Priority.MEDIUM, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.create(overTitle));

        UUID nonExistentCategory = UUID.randomUUID();
        TaskDraft invalidCategory = TaskDraft.ofSimple("Title", "Desc", Priority.MEDIUM, nonExistentCategory);
        ValidationException catEx = assertThrows(ValidationException.class, () -> taskService.create(invalidCategory));
        assertTrue(catEx.getMessage().contains("does not exist"));

        // No change events should be fired on validation failures
        assertTrue(eventLog.isEmpty());
    }

    // =========================================================================
    // 2. Read Task Tests
    // =========================================================================

    @Test
    @DisplayName("Get and getAll return defensive copies and handle missing entities")
    void testGetAndGetAll() {
        Task t1 = taskService.create(TaskDraft.ofSimple("Task 1", "Desc 1", Priority.HIGH, Category.DEFAULT_ID));
        Task t2 = taskService.create(TaskDraft.ofSimple("Task 2", "Desc 2", Priority.LOW, Category.DEFAULT_ID));

        Task fetched1 = taskService.get(t1.getId());
        assertEquals(t1.getId(), fetched1.getId());
        assertEquals(t1.getTitle(), fetched1.getTitle());

        // Mutating fetched copy does not affect stored state
        fetched1.setTitle("Hacked Title");
        assertEquals("Task 1", taskService.get(t1.getId()).getTitle());

        // getAll returns copies
        List<Task> all = taskService.getAll();
        assertEquals(2, all.size());
        all.get(0).setTitle("Hacked in list");
        assertEquals("Task 1", taskService.get(t1.getId()).getTitle());

        // Missing ID handling
        assertThrows(NotFoundException.class, () -> taskService.get(null));
        assertThrows(NotFoundException.class, () -> taskService.get(UUID.randomUUID()));
    }

    // =========================================================================
    // 3. Update Task Tests
    // =========================================================================

    @Test
    @DisplayName("Update task: success, timestamp update, and copy isolation")
    void testUpdateTaskSuccess() {
        Task created = taskService.create(TaskDraft.ofSimple("Initial Title", "Initial Desc", Priority.MEDIUM, Category.DEFAULT_ID));
        eventLog.clear();

        TaskDraft updateDraft = TaskDraft.ofSimple("Updated Title", "Updated Desc", Priority.HIGH, Category.DEFAULT_ID);
        Task updated = taskService.update(created.getId(), updateDraft);

        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Desc", updated.getDescription());
        assertEquals(Priority.HIGH, updated.getPriority());
        assertEquals(1, eventLog.size());
        assertTrue(eventLog.get(0).startsWith("UPDATED:" + created.getId()));

        // Verify stored task reflects update
        Task stored = taskService.get(created.getId());
        assertEquals("Updated Title", stored.getTitle());

        // Mutating returned update does not mutate stored task
        updated.setTitle("Mutated After Update");
        assertEquals("Updated Title", taskService.get(created.getId()).getTitle());
    }

    @Test
    @DisplayName("Update task: type mutation prohibited and validation checks")
    void testUpdateTaskValidation() {
        Task simple = taskService.create(TaskDraft.ofSimple("Simple Task", "Desc", Priority.MEDIUM, Category.DEFAULT_ID));

        // Prohibit changing task type from Simple to Deadline
        TaskDraft deadlineDraft = TaskDraft.ofDeadline("Try Changing Type", "Desc", Priority.MEDIUM, Category.DEFAULT_ID, LocalDate.now());
        ValidationException typeEx = assertThrows(ValidationException.class, () -> taskService.update(simple.getId(), deadlineDraft));
        assertTrue(typeEx.getMessage().contains("Task type cannot be changed"));

        // Invalid title in update draft
        TaskDraft invalidDraft = TaskDraft.ofSimple("", "Desc", Priority.MEDIUM, Category.DEFAULT_ID);
        assertThrows(ValidationException.class, () -> taskService.update(simple.getId(), invalidDraft));

        // Unknown category in update draft
        TaskDraft unknownCatDraft = TaskDraft.ofSimple("Valid Title", "Desc", Priority.MEDIUM, UUID.randomUUID());
        assertThrows(ValidationException.class, () -> taskService.update(simple.getId(), unknownCatDraft));

        // Non-existent task ID
        assertThrows(NotFoundException.class, () -> taskService.update(UUID.randomUUID(), TaskDraft.ofSimple("A", "B", Priority.LOW, Category.DEFAULT_ID)));
        assertThrows(NotFoundException.class, () -> taskService.update(null, TaskDraft.ofSimple("A", "B", Priority.LOW, Category.DEFAULT_ID)));
    }

    // =========================================================================
    // 4. Delete & Undo Buffer Tests
    // =========================================================================

    @Test
    @DisplayName("Delete task moves entity to undo buffer, undo restores once")
    void testDeleteAndUndoBuffer() {
        Task task1 = taskService.create(TaskDraft.ofSimple("Delete Me 1", "Desc", Priority.HIGH, Category.DEFAULT_ID));
        Task task2 = taskService.create(TaskDraft.ofSimple("Delete Me 2", "Desc", Priority.LOW, Category.DEFAULT_ID));
        eventLog.clear();

        // Delete task 1
        assertFalse(taskService.canUndo());
        Task deleted1 = taskService.delete(task1.getId());
        assertTrue(taskService.canUndo());
        assertEquals(task1.getId(), deleted1.getId());
        assertEquals(1, taskService.getAll().size());
        assertEquals(1, eventLog.size());
        assertTrue(eventLog.get(0).startsWith("DELETED:" + task1.getId()));
        assertThrows(NotFoundException.class, () -> taskService.get(task1.getId()));

        // Undo restores task 1
        Optional<Task> restored1 = taskService.undoDelete();
        assertFalse(taskService.canUndo());
        assertTrue(restored1.isPresent());
        assertEquals(task1.getId(), restored1.get().getId());
        assertEquals("Delete Me 1", restored1.get().getTitle());
        assertEquals(2, taskService.getAll().size());
        assertTrue(eventLog.get(1).startsWith("RESTORED:" + task1.getId()));

        // Second undo call returns empty (buffer cleared)
        Optional<Task> secondUndo = taskService.undoDelete();
        assertFalse(secondUndo.isPresent());

        // Deleting non-existent task
        assertThrows(NotFoundException.class, () -> taskService.delete(UUID.randomUUID()));
        assertThrows(NotFoundException.class, () -> taskService.delete(null));

        // Deleting task 2 overwrites buffer
        taskService.delete(task2.getId());
        Optional<Task> restored2 = taskService.undoDelete();
        assertTrue(restored2.isPresent());
        assertEquals(task2.getId(), restored2.get().getId());
    }

    @Test
    @DisplayName("Direct restore method")
    void testDirectRestore() {
        Task task = new SimpleTask("Standup", "Daily sync", Priority.HIGH, Category.DEFAULT_ID);
        Task restored = taskService.restore(task);
        assertEquals(task.getId(), restored.getId());
        assertEquals(1, taskService.getAll().size());
        assertThrows(NullPointerException.class, () -> taskService.restore(null));
    }

    // =========================================================================
    // 5. Priority, Completion & Pinning State Changes
    // =========================================================================

    @Test
    @DisplayName("Toggle complete, set priority and toggle pinned")
    void testTaskStateToggles() {
        Task task = taskService.create(TaskDraft.ofSimple("Toggles Task", "Desc", Priority.MEDIUM, Category.DEFAULT_ID));
        assertFalse(task.isCompleted());
        assertFalse(task.isPinned());

        // Toggle complete
        Task completed = taskService.toggleComplete(task.getId());
        assertTrue(completed.isCompleted());
        Task uncompleted = taskService.toggleComplete(task.getId());
        assertFalse(uncompleted.isCompleted());

        // Set priority
        Task high = taskService.setPriority(task.getId(), Priority.HIGH);
        assertEquals(Priority.HIGH, high.getPriority());
        assertThrows(ValidationException.class, () -> taskService.setPriority(task.getId(), null));

        // Toggle pinned
        Task pinned = taskService.togglePinned(task.getId());
        assertTrue(pinned.isPinned());
        Task unpinned = taskService.togglePinned(task.getId());
        assertFalse(unpinned.isPinned());

        // Not found checks
        UUID missing = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> taskService.toggleComplete(missing));
        assertThrows(NotFoundException.class, () -> taskService.setPriority(missing, Priority.LOW));
        assertThrows(NotFoundException.class, () -> taskService.togglePinned(missing));
        assertThrows(NotFoundException.class, () -> taskService.toggleComplete(null));
    }

    // =========================================================================
    // 6. Listener Isolation and Registration
    // =========================================================================

    @Test
    @DisplayName("Listener exception isolation and removeListener")
    void testListenerBehavior() {
        AtomicInteger count = new AtomicInteger();
        TaskChangeListener failingListener = (type, id) -> {
            throw new RuntimeException("Simulated observer crash");
        };
        TaskChangeListener normalListener = (type, id) -> count.incrementAndGet();

        taskService.addListener(failingListener);
        taskService.addListener(normalListener);

        assertDoesNotThrow(() -> taskService.create(TaskDraft.ofSimple("Safe", "Desc", Priority.LOW, Category.DEFAULT_ID)));
        assertEquals(1, count.get());

        // Remove listener
        taskService.removeListener(normalListener);
        taskService.create(TaskDraft.ofSimple("Another", "Desc", Priority.LOW, Category.DEFAULT_ID));
        assertEquals(1, count.get()); // No increment
    }

    @Test
    @DisplayName("Default constructor using system clock")
    void testDefaultConstructor() {
        TaskService defaultService = new TaskService(taskRepo, categoryRepo);
        assertNotNull(defaultService);
        Task t = defaultService.create(TaskDraft.ofSimple("Default Clock Task", "", Priority.LOW, Category.DEFAULT_ID));
        assertNotNull(t.getId());
    }
}
