package com.todoapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit test suite covering the domain model package.
 */
class TaskModelTest {

    // =========================================================================
    // 1. Priority Enum Tests
    // =========================================================================

    @Test
    @DisplayName("Priority rank, labels and parsing")
    void testPriorityEnum() {
        assertEquals(1, Priority.HIGH.getRank());
        assertEquals("High", Priority.HIGH.getLabel());
        assertEquals(2, Priority.MEDIUM.getRank());
        assertEquals(3, Priority.LOW.getRank());

        assertEquals(Priority.HIGH, Priority.fromLabel("High"));
        assertEquals(Priority.HIGH, Priority.fromLabel("high"));
        assertEquals(Priority.HIGH, Priority.fromLabel(" HIGH "));
        assertEquals(Priority.MEDIUM, Priority.fromLabel("Medium"));
        assertEquals(Priority.LOW, Priority.fromLabel("low"));

        assertThrows(IllegalArgumentException.class, () -> Priority.fromLabel(null));
        assertThrows(IllegalArgumentException.class, () -> Priority.fromLabel("   "));
        assertThrows(IllegalArgumentException.class, () -> Priority.fromLabel("Urgent"));
        assertEquals("High", Priority.HIGH.toString());
    }

    // =========================================================================
    // 2. TaskType Enum Tests
    // =========================================================================

    @Test
    @DisplayName("TaskType labels and parsing")
    void testTaskTypeEnum() {
        assertEquals("Simple", TaskType.SIMPLE.getLabel());
        assertEquals("Deadline", TaskType.DEADLINE.getLabel());

        assertEquals(TaskType.SIMPLE, TaskType.fromLabel("Simple"));
        assertEquals(TaskType.DEADLINE, TaskType.fromLabel(" DEADLINE "));

        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel(null));
        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel(""));
        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel("Recurring"));
        assertEquals("Simple", TaskType.SIMPLE.toString());
    }

    // =========================================================================
    // 3. Category Tests
    // =========================================================================

    @Test
    @DisplayName("Category creation, defaults and validation")
    void testCategory() {
        Category defaultCat = Category.createDefault();
        assertEquals(Category.DEFAULT_ID, defaultCat.getId());
        assertEquals("General", defaultCat.getName());
        assertEquals("#757575", defaultCat.getColorHex());

        Category custom = new Category("Academics", "#1976D2");
        assertNotNull(custom.getId());
        assertEquals("Academics", custom.getName());
        assertEquals("#1976D2", custom.getColorHex());

        custom.setName("Studies");
        assertEquals("Studies", custom.getName());
        custom.setColorHex("#388E3C");
        assertEquals("#388E3C", custom.getColorHex());

        // Name validation
        assertThrows(NullPointerException.class, () -> new Category(null, "Test", "#123456"));
        assertThrows(IllegalArgumentException.class, () -> new Category(UUID.randomUUID(), null, "#123456"));
        assertThrows(IllegalArgumentException.class, () -> new Category(UUID.randomUUID(), "   ", "#123456"));
        assertThrows(IllegalArgumentException.class, () -> new Category(UUID.randomUUID(), "a".repeat(31), "#123456"));
        assertThrows(IllegalArgumentException.class, () -> custom.setName(""));
        assertThrows(IllegalArgumentException.class, () -> custom.setName("b".repeat(31)));

        // Color validation
        assertThrows(IllegalArgumentException.class, () -> new Category("Test", "invalid-color"));
        assertThrows(IllegalArgumentException.class, () -> new Category("Test", "#12345"));
        assertThrows(IllegalArgumentException.class, () -> new Category("Test", "#1234567"));
        assertThrows(IllegalArgumentException.class, () -> custom.setColorHex(null));
        assertThrows(IllegalArgumentException.class, () -> custom.setColorHex("red"));

        Category sameId = new Category(custom.getId(), "Other", "#000000");
        assertEquals(custom, sameId);
        assertEquals(custom.hashCode(), sameId.hashCode());
        assertTrue(custom.toString().contains("Studies"));
    }

    // =========================================================================
    // 4. Abstract Task & SimpleTask Tests
    // =========================================================================

    @Test
    @DisplayName("SimpleTask attributes, progress and copy")
    void testSimpleTask() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        SimpleTask task = new SimpleTask(id, "Complete Lab",
                Priority.HIGH, Category.DEFAULT_ID, false, true, now, now);

        assertEquals(id, task.getId());
        assertEquals("Complete Lab", task.getTitle());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(Category.DEFAULT_ID, task.getCategoryId());
        assertFalse(task.isCompleted());
        assertTrue(task.isPinned());
        assertEquals(TaskType.SIMPLE, task.getType());
        assertEquals(Optional.empty(), task.getDueDate());
        assertFalse(task.isOverdue(LocalDate.now()));
        assertEquals(0.0, task.getProgress());
        assertEquals("", task.getSummary());

        // Title update
        task.setTitle("Updated Lab");
        assertEquals("Updated Lab", task.getTitle());

        // Priority and category updates
        task.setPriority(Priority.LOW);
        assertEquals(Priority.LOW, task.getPriority());
        UUID newCat = UUID.randomUUID();
        task.setCategoryId(newCat);
        assertEquals(newCat, task.getCategoryId());

        // Completion and pinning
        task.setCompleted(true);
        assertTrue(task.isCompleted());
        assertEquals(1.0, task.getProgress());
        task.setPinned(false);
        assertFalse(task.isPinned());

        // Copy semantics
        SimpleTask copy = task.copy();
        assertEquals(task.getId(), copy.getId());
        assertEquals(task.getTitle(), copy.getTitle());
        assertEquals(task.getPriority(), copy.getPriority());
        assertEquals(task.getCategoryId(), copy.getCategoryId());
        assertEquals(task.isCompleted(), copy.isCompleted());
        assertEquals(task.isPinned(), copy.isPinned());
        assertEquals(task, copy);

        // Validation bounds
        assertThrows(IllegalArgumentException.class, () -> task.setTitle(null));
        assertThrows(IllegalArgumentException.class, () -> task.setTitle("   "));
        assertThrows(IllegalArgumentException.class, () -> task.setTitle("x".repeat(101)));
        assertThrows(IllegalArgumentException.class, () -> task.setPriority(null));
        assertThrows(IllegalArgumentException.class, () -> task.setCategoryId(null));

        task.touch(now.plusDays(1));
        assertEquals(now.plusDays(1), task.getUpdatedAt());
        assertTrue(task.toString().contains("SimpleTask"));
    }

    // =========================================================================
    // 5. DeadlineTask Tests
    // =========================================================================

    @Test
    @DisplayName("DeadlineTask due dates, overdue logic, progress and copy")
    void testDeadlineTask() {
        LocalDate today = LocalDate.of(2026, 11, 20);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        DeadlineTask task = new DeadlineTask("Project Milestone", Priority.HIGH, Category.DEFAULT_ID, tomorrow);
        assertEquals(TaskType.DEADLINE, task.getType());
        assertEquals(tomorrow, task.getDueDateValue());
        assertEquals(Optional.of(tomorrow), task.getDueDate());
        assertFalse(task.isOverdue(today));
        assertEquals(0.0, task.getProgress());
        assertTrue(task.getSummary().startsWith("Due "));

        // Overdue check
        task.setDueDate(yesterday);
        assertTrue(task.isOverdue(today));

        // When completed, it is no longer overdue
        task.setCompleted(true);
        assertFalse(task.isOverdue(today));
        assertEquals(1.0, task.getProgress());

        // Reference date null check
        assertThrows(NullPointerException.class, () -> task.isOverdue(null));
        assertThrows(NullPointerException.class, () -> task.setDueDate(null));

        // Copy semantics
        DeadlineTask copy = task.copy();
        assertEquals(task.getId(), copy.getId());
        assertEquals(task.getDueDateValue(), copy.getDueDateValue());
        assertEquals(task, copy);
    }

    // =========================================================================
    // 6. Polymorphic Parameterized Test
    // =========================================================================

    static Stream<Task> taskProvider() {
        SimpleTask simple = new SimpleTask("Simple Task", Priority.LOW, Category.DEFAULT_ID);
        DeadlineTask deadline = new DeadlineTask("Deadline Task", Priority.HIGH, Category.DEFAULT_ID,
                LocalDate.of(2026, 12, 1));
        return Stream.of(simple, deadline);
    }

    @ParameterizedTest
    @MethodSource("taskProvider")
    @DisplayName("Polymorphic invocation on both Task subtypes")
    void testPolymorphicTaskBehavior(Task task) {
        assertNotNull(task.getType());
        assertNotNull(task.getSummary());
        assertTrue(task.getProgress() >= 0.0 && task.getProgress() <= 1.0);
        assertNotNull(task.getDueDate());

        Task copy = task.copy();
        assertEquals(task.getId(), copy.getId());
        assertEquals(task.getType(), copy.getType());
        assertEquals(task.getSummary(), copy.getSummary());
        assertEquals(task.getProgress(), copy.getProgress());
        assertEquals(task, copy);
    }

    // =========================================================================
    // 7. TaskDraft & TaskFactory Tests
    // =========================================================================

    @Test
    @DisplayName("TaskDraft validation and TaskFactory create/apply")
    void testTaskDraftAndFactory() {
        // Draft validation
        TaskDraft validSimple = TaskDraft.ofSimple("Title", Priority.HIGH, Category.DEFAULT_ID);
        assertDoesNotThrow(validSimple::validate);

        assertThrows(IllegalArgumentException.class, () ->
                new TaskDraft(null, "Title", Priority.HIGH, Category.DEFAULT_ID, null).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofSimple("", Priority.HIGH, Category.DEFAULT_ID).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofSimple("x".repeat(101), Priority.HIGH, Category.DEFAULT_ID).validate());
        assertThrows(IllegalArgumentException.class, () ->
                new TaskDraft(TaskType.DEADLINE, "Title", Priority.HIGH, Category.DEFAULT_ID, null).validate());

        // Factory create
        Task createdSimple = TaskFactory.create(validSimple);
        assertInstanceOf(SimpleTask.class, createdSimple);
        assertEquals("Title", createdSimple.getTitle());

        TaskDraft validDeadline = TaskDraft.ofDeadline("Deadline", Priority.LOW, Category.DEFAULT_ID, LocalDate.of(2026, 12, 31));
        Task createdDeadline = TaskFactory.create(validDeadline);
        assertInstanceOf(DeadlineTask.class, createdDeadline);
        assertEquals(LocalDate.of(2026, 12, 31), ((DeadlineTask) createdDeadline).getDueDateValue());

        // Factory apply
        TaskDraft updateDraft = TaskDraft.ofSimple("Updated Title", Priority.LOW, Category.DEFAULT_ID);
        TaskFactory.apply(createdSimple, updateDraft);
        assertEquals("Updated Title", createdSimple.getTitle());
        assertEquals(Priority.LOW, createdSimple.getPriority());

        // Applying different type must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> TaskFactory.apply(createdSimple, validDeadline));

        // Applying deadline update
        TaskDraft updateDeadlineDraft = TaskDraft.ofDeadline("New Deadline Title", Priority.HIGH, Category.DEFAULT_ID, LocalDate.of(2027, 1, 1));
        TaskFactory.apply(createdDeadline, updateDeadlineDraft);
        assertEquals(LocalDate.of(2027, 1, 1), ((DeadlineTask) createdDeadline).getDueDateValue());
    }
}
