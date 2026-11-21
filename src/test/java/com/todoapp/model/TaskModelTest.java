package com.todoapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
        assertEquals("Checklist", TaskType.CHECKLIST.getLabel());
        assertEquals("Deadline", TaskType.DEADLINE.getLabel());

        assertEquals(TaskType.SIMPLE, TaskType.fromLabel("Simple"));
        assertEquals(TaskType.CHECKLIST, TaskType.fromLabel("checklist"));
        assertEquals(TaskType.DEADLINE, TaskType.fromLabel(" DEADLINE "));

        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel(null));
        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel(""));
        assertThrows(IllegalArgumentException.class, () -> TaskType.fromLabel("Recurring"));
        assertEquals("Simple", TaskType.SIMPLE.toString());
    }

    // =========================================================================
    // 3. ChecklistItem Tests
    // =========================================================================

    @Test
    @DisplayName("ChecklistItem validation, getters, setters and copy")
    void testChecklistItem() {
        UUID id = UUID.randomUUID();
        ChecklistItem item = new ChecklistItem(id, "Buy coffee beans", false);

        assertEquals(id, item.getId());
        assertEquals("Buy coffee beans", item.getText());
        assertFalse(item.isDone());

        item.setDone(true);
        assertTrue(item.isDone());

        item.setText("Buy dark roast coffee");
        assertEquals("Buy dark roast coffee", item.getText());

        // Convenience constructor
        ChecklistItem autoItem = new ChecklistItem("Read chapter 1");
        assertNotNull(autoItem.getId());
        assertFalse(autoItem.isDone());

        // Copy semantics
        ChecklistItem copy = item.copy();
        assertEquals(item.getId(), copy.getId());
        assertEquals(item.getText(), copy.getText());
        assertEquals(item.isDone(), copy.isDone());
        assertEquals(item, copy);
        assertEquals(item.hashCode(), copy.hashCode());

        // Mutating copy does not mutate original
        copy.setText("Different text");
        copy.setDone(false);
        assertEquals("Buy dark roast coffee", item.getText());
        assertTrue(item.isDone());

        // Validations
        assertThrows(NullPointerException.class, () -> new ChecklistItem(null, "Text", false));
        assertThrows(IllegalArgumentException.class, () -> new ChecklistItem(UUID.randomUUID(), null, false));
        assertThrows(IllegalArgumentException.class, () -> new ChecklistItem(UUID.randomUUID(), "   ", false));
        assertThrows(IllegalArgumentException.class, () -> new ChecklistItem(UUID.randomUUID(), "a".repeat(101), false));
        assertThrows(IllegalArgumentException.class, () -> item.setText(""));
        assertThrows(IllegalArgumentException.class, () -> item.setText("x".repeat(101)));
        assertTrue(item.toString().contains("Buy dark roast coffee"));
    }

    // =========================================================================
    // 4. Category Tests
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
    // 5. Abstract Task & SimpleTask Tests
    // =========================================================================

    @Test
    @DisplayName("SimpleTask attributes, summary truncation, progress and copy")
    void testSimpleTask() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        SimpleTask task = new SimpleTask(id, "Complete Lab", "A short description",
                Priority.HIGH, Category.DEFAULT_ID, false, true, now, now);

        assertEquals(id, task.getId());
        assertEquals("Complete Lab", task.getTitle());
        assertEquals("A short description", task.getDescription());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(Category.DEFAULT_ID, task.getCategoryId());
        assertFalse(task.isCompleted());
        assertTrue(task.isPinned());
        assertEquals(TaskType.SIMPLE, task.getType());
        assertEquals(Optional.empty(), task.getDueDate());
        assertFalse(task.isOverdue(LocalDate.now()));
        assertEquals(0.0, task.getProgress());
        assertEquals("A short description", task.getSummary());

        // Title and description updates
        task.setTitle("Updated Lab");
        assertEquals("Updated Lab", task.getTitle());
        task.setDescription("New description");
        assertEquals("New description", task.getDescription());
        task.setDescription(null);
        assertEquals("", task.getDescription());
        assertEquals("", task.getSummary());

        // Summary truncation at 80 characters
        task.setDescription("a".repeat(80));
        assertEquals("a".repeat(80), task.getSummary());
        task.setDescription("a".repeat(81));
        assertEquals("a".repeat(77) + "...", task.getSummary());

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
        assertEquals(task.getDescription(), copy.getDescription());
        assertEquals(task.getPriority(), copy.getPriority());
        assertEquals(task.getCategoryId(), copy.getCategoryId());
        assertEquals(task.isCompleted(), copy.isCompleted());
        assertEquals(task.isPinned(), copy.isPinned());
        assertEquals(task, copy);

        // Validation bounds
        assertThrows(IllegalArgumentException.class, () -> task.setTitle(null));
        assertThrows(IllegalArgumentException.class, () -> task.setTitle("   "));
        assertThrows(IllegalArgumentException.class, () -> task.setTitle("x".repeat(101)));
        assertThrows(IllegalArgumentException.class, () -> task.setDescription("d".repeat(2001)));
        assertThrows(IllegalArgumentException.class, () -> task.setPriority(null));
        assertThrows(IllegalArgumentException.class, () -> task.setCategoryId(null));

        task.touch(now.plusDays(1));
        assertEquals(now.plusDays(1), task.getUpdatedAt());
        assertTrue(task.toString().contains("SimpleTask"));
    }

    // =========================================================================
    // 6. DeadlineTask Tests
    // =========================================================================

    @Test
    @DisplayName("DeadlineTask due dates, overdue logic, progress and copy")
    void testDeadlineTask() {
        LocalDate today = LocalDate.of(2026, 11, 20);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);

        DeadlineTask task = new DeadlineTask("Project Milestone", "Due soon", Priority.HIGH, Category.DEFAULT_ID, tomorrow);
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
    // 7. ChecklistTask & State Transition Tests
    // =========================================================================

    @Test
    @DisplayName("ChecklistTask items, four completion transitions and deep copy")
    void testChecklistTask() {
        ChecklistTask task = new ChecklistTask("Grocery List", "Buy provisions", Priority.MEDIUM, Category.DEFAULT_ID, null);
        assertEquals(TaskType.CHECKLIST, task.getType());
        assertEquals(Optional.empty(), task.getDueDate());
        assertFalse(task.isOverdue(LocalDate.now()));
        assertEquals(0.0, task.getProgress());
        assertEquals("0 items", task.getSummary());

        // Add items
        ChecklistItem item1 = task.addItem("Apples");
        ChecklistItem item2 = task.addItem("Milk");
        ChecklistItem item3 = task.addItem("Bread");
        assertEquals(3, task.getItems().size());
        assertEquals("0/3 done", task.getSummary());
        assertEquals(0.0, task.getProgress());
        assertFalse(task.isCompleted());

        // Transition 1: Partial completion
        task.toggleItem(item1.getId());
        assertEquals("1/3 done", task.getSummary());
        assertEquals(1.0 / 3.0, task.getProgress(), 0.001);
        assertFalse(task.isCompleted());

        task.toggleItem(item2.getId());
        assertEquals("2/3 done", task.getSummary());
        assertFalse(task.isCompleted());

        // Transition 2: Checking last item auto-completes parent task
        task.toggleItem(item3.getId());
        assertEquals("3/3 done", task.getSummary());
        assertEquals(1.0, task.getProgress());
        assertTrue(task.isCompleted());

        // Transition 3: Unchecking any item auto-uncompletes parent task
        task.toggleItem(item2.getId());
        assertEquals("2/3 done", task.getSummary());
        assertFalse(task.isCompleted());

        // Transition 4: setCompleted(true) marks all items done; setCompleted(false) marks all items false
        task.setCompleted(true);
        assertTrue(task.isCompleted());
        assertTrue(task.getItems().stream().allMatch(ChecklistItem::isDone));

        task.setCompleted(false);
        assertFalse(task.isCompleted());
        assertTrue(task.getItems().stream().noneMatch(ChecklistItem::isDone));

        // Adding an item to a completed task uncompletes it
        task.setCompleted(true);
        task.addItem("Eggs");
        assertFalse(task.isCompleted());
        assertEquals("4 items", task.getItems().size() + " items");

        // Remove item
        task.removeItem(item1.getId());
        assertEquals(3, task.getItems().size());
        assertFalse(task.removeItem(UUID.randomUUID()));

        // Toggle non-existent item
        assertFalse(task.toggleItem(UUID.randomUUID()));

        // Deep copy isolation
        ChecklistTask copy = task.copy();
        assertEquals(task.getId(), copy.getId());
        assertEquals(task.getItems().size(), copy.getItems().size());
        assertEquals(task, copy);

        // Mutating copy's items does not affect original task
        copy.getItems().get(0).setText("Modified in copy");
        copy.addItem("New item in copy");
        assertNotEquals(copy.getItems().size(), task.getItems().size());
        assertNotEquals("Modified in copy", task.getItems().get(0).getText());

        // syncItems
        task.syncItems(List.of("Milk", "Bread", "Butter"));
        assertEquals(3, task.getItems().size());
        assertEquals("Milk", task.getItems().get(0).getText());
    }

    // =========================================================================
    // 8. Polymorphic Parameterized Test
    // =========================================================================

    static Stream<Task> taskProvider() {
        SimpleTask simple = new SimpleTask("Simple Task", "A simple note", Priority.LOW, Category.DEFAULT_ID);
        ChecklistTask checklist = new ChecklistTask("Checklist Task", "Checklist note", Priority.MEDIUM, Category.DEFAULT_ID,
                List.of(new ChecklistItem("Item A"), new ChecklistItem("Item B")));
        DeadlineTask deadline = new DeadlineTask("Deadline Task", "Deadline note", Priority.HIGH, Category.DEFAULT_ID,
                LocalDate.of(2026, 12, 1));
        return Stream.of(simple, checklist, deadline);
    }

    @ParameterizedTest
    @MethodSource("taskProvider")
    @DisplayName("Polymorphic invocation on all three Task subtypes")
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
    // 9. TaskDraft & TaskFactory Tests
    // =========================================================================

    @Test
    @DisplayName("TaskDraft validation and TaskFactory create/apply")
    void testTaskDraftAndFactory() {
        // Draft validation
        TaskDraft validSimple = TaskDraft.ofSimple("Title", "Desc", Priority.HIGH, Category.DEFAULT_ID);
        assertDoesNotThrow(validSimple::validate);

        assertThrows(IllegalArgumentException.class, () ->
                new TaskDraft(null, "Title", "Desc", Priority.HIGH, Category.DEFAULT_ID, null, List.of()).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofSimple("", "Desc", Priority.HIGH, Category.DEFAULT_ID).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofSimple("x".repeat(101), "Desc", Priority.HIGH, Category.DEFAULT_ID).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofSimple("Title", "x".repeat(2001), Priority.HIGH, Category.DEFAULT_ID).validate());
        assertThrows(IllegalArgumentException.class, () ->
                new TaskDraft(TaskType.DEADLINE, "Title", "Desc", Priority.HIGH, Category.DEFAULT_ID, null, List.of()).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofChecklist("Title", "Desc", Priority.HIGH, Category.DEFAULT_ID, List.of("")).validate());
        assertThrows(IllegalArgumentException.class, () ->
                TaskDraft.ofChecklist("Title", "Desc", Priority.HIGH, Category.DEFAULT_ID, List.of("a".repeat(101))).validate());

        // Factory create
        Task createdSimple = TaskFactory.create(validSimple);
        assertInstanceOf(SimpleTask.class, createdSimple);
        assertEquals("Title", createdSimple.getTitle());

        TaskDraft validChecklist = TaskDraft.ofChecklist("Checklist", "Desc", Priority.MEDIUM, Category.DEFAULT_ID, List.of("Sub 1", "Sub 2"));
        Task createdChecklist = TaskFactory.create(validChecklist);
        assertInstanceOf(ChecklistTask.class, createdChecklist);
        assertEquals(2, ((ChecklistTask) createdChecklist).getItems().size());

        TaskDraft validDeadline = TaskDraft.ofDeadline("Deadline", "Desc", Priority.LOW, Category.DEFAULT_ID, LocalDate.of(2026, 12, 31));
        Task createdDeadline = TaskFactory.create(validDeadline);
        assertInstanceOf(DeadlineTask.class, createdDeadline);
        assertEquals(LocalDate.of(2026, 12, 31), ((DeadlineTask) createdDeadline).getDueDateValue());

        // Factory apply
        TaskDraft updateDraft = TaskDraft.ofSimple("Updated Title", "Updated Desc", Priority.LOW, Category.DEFAULT_ID);
        TaskFactory.apply(createdSimple, updateDraft);
        assertEquals("Updated Title", createdSimple.getTitle());
        assertEquals("Updated Desc", createdSimple.getDescription());
        assertEquals(Priority.LOW, createdSimple.getPriority());

        // Applying different type must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> TaskFactory.apply(createdSimple, validDeadline));

        // Applying deadline update
        TaskDraft updateDeadlineDraft = TaskDraft.ofDeadline("New Deadline Title", "Desc", Priority.HIGH, Category.DEFAULT_ID, LocalDate.of(2027, 1, 1));
        TaskFactory.apply(createdDeadline, updateDeadlineDraft);
        assertEquals(LocalDate.of(2027, 1, 1), ((DeadlineTask) createdDeadline).getDueDateValue());

        // Applying checklist update
        TaskDraft updateChecklistDraft = TaskDraft.ofChecklist("New Checklist", "Desc", Priority.MEDIUM, Category.DEFAULT_ID, List.of("Sub 1", "Sub 3"));
        TaskFactory.apply(createdChecklist, updateChecklistDraft);
        assertEquals(2, ((ChecklistTask) createdChecklist).getItems().size());
    }
}
