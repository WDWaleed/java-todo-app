package com.todoapp.controller;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import com.todoapp.service.CategoryService;
import com.todoapp.service.SortOption;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskQuery;
import com.todoapp.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainController verifying query state transitions,
 * listener notification contract, and message handling without JavaFX dependencies.
 */
class MainControllerTest {

    private InMemoryTaskRepository taskRepo;
    private InMemoryCategoryRepository categoryRepo;
    private TaskService taskService;
    private CategoryService categoryService;
    private MainController controller;

    private List<List<Task>> receivedTasks;
    private List<List<Category>> receivedCategories;
    private List<ViewState> receivedStates;

    @BeforeEach
    void setUp() {
        taskRepo = new InMemoryTaskRepository();
        categoryRepo = new InMemoryCategoryRepository();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), ZoneId.of("UTC"));
        taskService = new TaskService(taskRepo, categoryRepo, fixedClock);
        categoryService = new CategoryService(categoryRepo, taskRepo);
        controller = new MainController(taskService, categoryService);

        receivedTasks = new ArrayList<>();
        receivedCategories = new ArrayList<>();
        receivedStates = new ArrayList<>();

        controller.addListener((tasks, categories, state) -> {
            receivedTasks.add(tasks);
            receivedCategories.add(categories);
            receivedStates.add(state);
        });
    }

    @Test
    @DisplayName("Initial state has default query and no messages")
    void testInitialState() {
        ViewState initial = controller.getCurrentState();
        assertNotNull(initial);
        assertEquals(TaskQuery.defaultQuery(), initial.query());
        assertNull(initial.selectedTaskId());
        assertNull(initial.infoMessage());
        assertNull(initial.errorMessage());
        assertSame(taskService, controller.getTaskService());
        assertSame(categoryService, controller.getCategoryService());
    }

    @Test
    @DisplayName("refresh() queries services and notifies view listeners")
    void testRefreshNotifiesListeners() {
        taskService.create(TaskDraft.ofSimple("Task 1", "Desc 1", Priority.HIGH, Category.DEFAULT_ID));
        receivedTasks.clear();
        receivedCategories.clear();

        controller.refresh();

        assertEquals(1, receivedTasks.size());
        assertEquals(1, receivedTasks.get(0).size());
        assertEquals("Task 1", receivedTasks.get(0).get(0).getTitle());
        assertEquals(1, receivedCategories.get(0).size()); // General
    }

    @Test
    @DisplayName("onSelectCategory updates category filter and notifies once")
    void testOnSelectCategory() {
        Category work = categoryService.create("Work", "#1976D2");
        Category personal = categoryService.create("Personal", "#388E3C");

        taskService.create(TaskDraft.ofSimple("Work Task", "", Priority.HIGH, work.getId()));
        taskService.create(TaskDraft.ofSimple("Personal Task", "", Priority.LOW, personal.getId()));

        receivedTasks.clear();
        receivedStates.clear();

        controller.onSelectCategory(work.getId());

        assertEquals(1, receivedTasks.size());
        assertEquals(1, receivedTasks.get(0).size());
        assertEquals("Work Task", receivedTasks.get(0).get(0).getTitle());
        assertEquals(work.getId(), receivedStates.get(0).query().categoryId());

        // Selecting null returns all tasks
        controller.onSelectCategory(null);
        assertEquals(2, receivedTasks.size());
        assertEquals(2, receivedTasks.get(1).size());
        assertNull(receivedStates.get(1).query().categoryId());
    }

    @Test
    @DisplayName("onSearch updates search query and filters live")
    void testOnSearch() {
        taskService.create(TaskDraft.ofSimple("Buy apples", "From store", Priority.MEDIUM, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Buy bananas", "From fruit market", Priority.MEDIUM, Category.DEFAULT_ID));

        receivedTasks.clear();

        controller.onSearch("apples");

        assertEquals(1, receivedTasks.size());
        assertEquals(1, receivedTasks.get(0).size());
        assertEquals("Buy apples", receivedTasks.get(0).get(0).getTitle());
    }

    @Test
    @DisplayName("onSort updates sort strategy and direction")
    void testOnSort() {
        taskService.create(TaskDraft.ofSimple("Alpha", "", Priority.LOW, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Beta", "", Priority.HIGH, Category.DEFAULT_ID));

        receivedTasks.clear();
        receivedStates.clear();

        controller.onSort(SortOption.TITLE, true);

        assertEquals(1, receivedTasks.size());
        assertEquals(SortOption.TITLE, receivedStates.get(0).query().sort());
        assertTrue(receivedStates.get(0).query().ascending());
        assertEquals("Alpha", receivedTasks.get(0).get(0).getTitle());

        controller.onSort(SortOption.TITLE, false);
        assertEquals("Beta", receivedTasks.get(1).get(0).getTitle());
    }

    @Test
    @DisplayName("onStatusFilter updates completion filter")
    void testOnStatusFilter() {
        Task t1 = taskService.create(TaskDraft.ofSimple("Active", "", Priority.MEDIUM, Category.DEFAULT_ID));
        Task t2 = taskService.create(TaskDraft.ofSimple("Done", "", Priority.MEDIUM, Category.DEFAULT_ID));
        taskService.toggleComplete(t2.getId());

        receivedTasks.clear();

        controller.onStatusFilter(StatusFilter.ACTIVE);
        assertEquals(1, receivedTasks.get(0).size());
        assertEquals("Active", receivedTasks.get(0).get(0).getTitle());

        controller.onStatusFilter(StatusFilter.COMPLETED);
        assertEquals(1, receivedTasks.get(1).size());
        assertEquals("Done", receivedTasks.get(1).get(0).getTitle());
    }

    @Test
    @DisplayName("onPriorityFilter filters by priority and clears on null")
    void testOnPriorityFilter() {
        taskService.create(TaskDraft.ofSimple("High task", "", Priority.HIGH, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Low task", "", Priority.LOW, Category.DEFAULT_ID));

        receivedTasks.clear();

        controller.onPriorityFilter(Priority.HIGH);
        assertEquals(1, receivedTasks.get(0).size());
        assertEquals("High task", receivedTasks.get(0).get(0).getTitle());

        controller.onPriorityFilter(null);
        assertEquals(2, receivedTasks.get(1).size());
    }

    @Test
    @DisplayName("onSelectTask updates selected task ID")
    void testOnSelectTask() {
        UUID id = UUID.randomUUID();
        controller.onSelectTask(id);

        assertEquals(id, controller.getCurrentState().selectedTaskId());
    }

    @Test
    @DisplayName("Message handling sets and clears info/error messages")
    void testMessageHandling() {
        controller.setInfoMessage("Task saved successfully");
        assertEquals("Task saved successfully", controller.getCurrentState().infoMessage());
        assertNull(controller.getCurrentState().errorMessage());

        controller.setErrorMessage("Failed to perform action");
        assertEquals("Failed to perform action", controller.getCurrentState().errorMessage());
        assertNull(controller.getCurrentState().infoMessage());

        controller.clearMessages();
        assertNull(controller.getCurrentState().infoMessage());
        assertNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("Domain mutations in task or category service trigger automatic controller refresh")
    void testDomainMutationsTriggerRefresh() {
        receivedTasks.clear();

        taskService.create(TaskDraft.ofSimple("Auto Refreshed Task", "", Priority.HIGH, Category.DEFAULT_ID));

        assertFalse(receivedTasks.isEmpty());
        assertTrue(receivedTasks.get(receivedTasks.size() - 1).stream()
                .anyMatch(t -> t.getTitle().equals("Auto Refreshed Task")));

        categoryService.create("Auto Cat", "#FB8C00");
        assertTrue(receivedCategories.get(receivedCategories.size() - 1).stream()
                .anyMatch(c -> c.getName().equals("Auto Cat")));
    }

    @Test
    @DisplayName("removeListener unregisters listener")
    void testRemoveListener() {
        AtomicInteger count = new AtomicInteger(0);
        ViewListener l = (t, c, s) -> count.incrementAndGet();

        controller.addListener(l);
        controller.refresh();
        assertEquals(1, count.get());

        controller.removeListener(l);
        controller.refresh();
        assertEquals(1, count.get());
    }

    @Test
    @DisplayName("ViewState record mutation helpers")
    void testViewStateHelpers() {
        ViewState s = ViewState.initial();
        UUID taskId = UUID.randomUUID();

        ViewState s1 = s.withSelectedTaskId(taskId);
        assertEquals(taskId, s1.selectedTaskId());

        ViewState s2 = s.withInfoMessage("Info");
        assertEquals("Info", s2.infoMessage());

        ViewState s3 = s.withErrorMessage("Error");
        assertEquals("Error", s3.errorMessage());

        ViewState s4 = s3.clearMessages();
        assertNull(s4.errorMessage());
        assertNull(s4.infoMessage());
    }

    @Test
    @DisplayName("onCreateTask succeeds with valid draft and updates state")
    void testOnCreateTaskSuccess() {
        TaskDraft draft = TaskDraft.ofSimple("Buy groceries", "Milk and bread", Priority.HIGH, Category.DEFAULT_ID);
        var createdOpt = controller.onCreateTask(draft);

        assertTrue(createdOpt.isPresent());
        Task created = createdOpt.get();
        assertEquals("Buy groceries", created.getTitle());
        assertEquals(created.getId(), controller.getCurrentState().selectedTaskId());
        assertTrue(controller.getCurrentState().infoMessage().contains("Task created: Buy groceries"));
        assertNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onCreateTask handles ValidationException and sets error message")
    void testOnCreateTaskValidationFailure() {
        TaskDraft invalidDraft = TaskDraft.ofSimple("", "Desc", Priority.MEDIUM, Category.DEFAULT_ID);
        var createdOpt = controller.onCreateTask(invalidDraft);

        assertTrue(createdOpt.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());
        assertFalse(controller.getCurrentState().errorMessage().isBlank());
    }

    @Test
    @DisplayName("onUpdateTask succeeds and updates task and info message")
    void testOnUpdateTaskSuccess() {
        Task created = taskService.create(TaskDraft.ofSimple("Initial", "Desc", Priority.LOW, Category.DEFAULT_ID));

        TaskDraft updatedDraft = TaskDraft.ofSimple("Updated Title", "Updated Desc", Priority.HIGH, Category.DEFAULT_ID);
        var updatedOpt = controller.onUpdateTask(created.getId(), updatedDraft);

        assertTrue(updatedOpt.isPresent());
        assertEquals("Updated Title", updatedOpt.get().getTitle());
        assertEquals(Priority.HIGH, updatedOpt.get().getPriority());
        assertEquals(created.getId(), controller.getCurrentState().selectedTaskId());
        assertTrue(controller.getCurrentState().infoMessage().contains("Task updated: Updated Title"));
    }

    @Test
    @DisplayName("onUpdateTask handles validation and not-found failure paths")
    void testOnUpdateTaskFailures() {
        Task created = taskService.create(TaskDraft.ofSimple("Initial", "Desc", Priority.LOW, Category.DEFAULT_ID));

        // Validation failure
        TaskDraft invalidDraft = TaskDraft.ofSimple("   ", "Desc", Priority.LOW, Category.DEFAULT_ID);
        var resultInvalid = controller.onUpdateTask(created.getId(), invalidDraft);
        assertTrue(resultInvalid.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());

        // Not found failure
        UUID randomId = UUID.randomUUID();
        var resultNotFound = controller.onUpdateTask(randomId, TaskDraft.ofSimple("Valid", "Desc", Priority.LOW, Category.DEFAULT_ID));
        assertTrue(resultNotFound.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onDeleteTask removes task, buffers for undo, and handles not-found")
    void testOnDeleteTask() {
        Task created = taskService.create(TaskDraft.ofSimple("To Delete", "Desc", Priority.MEDIUM, Category.DEFAULT_ID));

        boolean deleted = controller.onDeleteTask(created.getId());
        assertTrue(deleted);
        assertNull(controller.getCurrentState().selectedTaskId());
        assertTrue(controller.getCurrentState().infoMessage().contains("Task deleted: To Delete — Undo"));

        // Deleting non-existent task returns false and sets error message
        boolean deletedAgain = controller.onDeleteTask(created.getId());
        assertFalse(deletedAgain);
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onUndoDelete restores deleted task or reports nothing to undo")
    void testOnUndoDelete() {
        // When buffer is empty
        assertFalse(controller.canUndo());
        var emptyUndo = controller.onUndoDelete();
        assertTrue(emptyUndo.isEmpty());
        assertEquals("Nothing to undo", controller.getCurrentState().infoMessage());

        // After deleting a task
        Task created = taskService.create(TaskDraft.ofSimple("Undo Me", "", Priority.HIGH, Category.DEFAULT_ID));
        controller.onDeleteTask(created.getId());
        assertTrue(controller.canUndo());

        var restoredOpt = controller.onUndoDelete();
        assertFalse(controller.canUndo());
        assertTrue(restoredOpt.isPresent());
        assertEquals(created.getId(), restoredOpt.get().getId());
        assertEquals("Undo Me", restoredOpt.get().getTitle());
        assertTrue(controller.getCurrentState().infoMessage().contains("Task restored: Undo Me"));
    }

    @Test
    @DisplayName("onToggleComplete toggles status and handles not found")
    void testOnToggleComplete() {
        Task created = taskService.create(TaskDraft.ofSimple("Task", "", Priority.MEDIUM, Category.DEFAULT_ID));
        assertFalse(created.isCompleted());

        controller.onToggleComplete(created.getId());
        Task updated = taskService.get(created.getId());
        assertTrue(updated.isCompleted());
        assertNull(controller.getCurrentState().errorMessage());

        // Not found
        controller.onToggleComplete(UUID.randomUUID());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onSetPriority updates priority and handles not found")
    void testOnSetPriority() {
        Task created = taskService.create(TaskDraft.ofSimple("Task", "", Priority.LOW, Category.DEFAULT_ID));
        assertEquals(Priority.LOW, created.getPriority());

        controller.onSetPriority(created.getId(), Priority.HIGH);
        Task updated = taskService.get(created.getId());
        assertEquals(Priority.HIGH, updated.getPriority());
        assertNull(controller.getCurrentState().errorMessage());

        // Not found
        controller.onSetPriority(UUID.randomUUID(), Priority.HIGH);
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onTogglePinned toggles pinned state and handles not found")
    void testOnTogglePinned() {
        Task created = taskService.create(TaskDraft.ofSimple("Task", "", Priority.LOW, Category.DEFAULT_ID));
        assertFalse(created.isPinned());

        controller.onTogglePinned(created.getId());
        Task updated = taskService.get(created.getId());
        assertTrue(updated.isPinned());
        assertNull(controller.getCurrentState().errorMessage());

        // Not found
        controller.onTogglePinned(UUID.randomUUID());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onCreateCategory succeeds and sets info message")
    void testOnCreateCategorySuccess() {
        var createdOpt = controller.onCreateCategory("Work", "#1976D2");
        assertTrue(createdOpt.isPresent());
        Category created = createdOpt.get();
        assertEquals("Work", created.getName());
        assertEquals("#1976D2", created.getColorHex());
        assertTrue(controller.getCurrentState().infoMessage().contains("Category created: Work"));
        assertNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onCreateCategory handles validation failure and sets error message")
    void testOnCreateCategoryValidationFailure() {
        var createdBlank = controller.onCreateCategory("", "#1976D2");
        assertTrue(createdBlank.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());

        var createdInvalidColor = controller.onCreateCategory("Health", "invalid-color");
        assertTrue(createdInvalidColor.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onRenameCategory succeeds and handles failure cases")
    void testOnRenameCategory() {
        Category created = categoryService.create("Study", "#7B1FA2");

        var renamedOpt = controller.onRenameCategory(created.getId(), "Academics");
        assertTrue(renamedOpt.isPresent());
        assertEquals("Academics", renamedOpt.get().getName());
        assertTrue(controller.getCurrentState().infoMessage().contains("Category renamed: Academics"));

        // General cannot be renamed
        var renameGeneral = controller.onRenameCategory(Category.DEFAULT_ID, "New General");
        assertTrue(renameGeneral.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());

        // Blank name
        var renameBlank = controller.onRenameCategory(created.getId(), "   ");
        assertTrue(renameBlank.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());

        // Non-existent ID
        var renameNotFound = controller.onRenameCategory(UUID.randomUUID(), "Valid");
        assertTrue(renameNotFound.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onRecolorCategory updates color and handles failures")
    void testOnRecolorCategory() {
        Category created = categoryService.create("Fitness", "#388E3C");

        var recoloredOpt = controller.onRecolorCategory(created.getId(), "#F57C00");
        assertTrue(recoloredOpt.isPresent());
        assertEquals("#F57C00", recoloredOpt.get().getColorHex());
        assertTrue(controller.getCurrentState().infoMessage().contains("Category recolored: Fitness"));

        // Invalid color code
        var recolorInvalid = controller.onRecolorCategory(created.getId(), "bad");
        assertTrue(recolorInvalid.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());

        // Not found
        var recolorNotFound = controller.onRecolorCategory(UUID.randomUUID(), "#388E3C");
        assertTrue(recolorNotFound.isEmpty());
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onDeleteCategory deletes category, reassigns tasks, and falls back to All if selected")
    void testOnDeleteCategorySuccess() {
        Category projectCat = categoryService.create("Project", "#0097A7");
        Task task = taskService.create(TaskDraft.ofSimple("Project Task", "", Priority.HIGH, projectCat.getId()));

        // Select the category first
        controller.onSelectCategory(projectCat.getId());
        assertEquals(projectCat.getId(), controller.getCurrentState().query().categoryId());

        // Delete the category
        boolean deleted = controller.onDeleteCategory(projectCat.getId());
        assertTrue(deleted);
        assertTrue(controller.getCurrentState().infoMessage().contains("Category deleted: Project"));

        // Selection must fall back to "All" (null category ID)
        assertNull(controller.getCurrentState().query().categoryId());

        // Task must be reassigned to General
        Task updatedTask = taskService.get(task.getId());
        assertEquals(Category.DEFAULT_ID, updatedTask.getCategoryId());
    }

    @Test
    @DisplayName("onDeleteCategory handles default category and non-existent failures")
    void testOnDeleteCategoryFailures() {
        // Attempting to delete General category fails
        boolean deleteGeneral = controller.onDeleteCategory(Category.DEFAULT_ID);
        assertFalse(deleteGeneral);
        assertNotNull(controller.getCurrentState().errorMessage());

        // Non-existent ID fails
        boolean deleteNotFound = controller.onDeleteCategory(UUID.randomUUID());
        assertFalse(deleteNotFound);
        assertNotNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("onClearFilters resets all query criteria to defaults")
    void testOnClearFilters() {
        Category cat = categoryService.create("Personal", "#E91E63");
        controller.onSelectCategory(cat.getId());
        controller.onSearch("test search");
        controller.onStatusFilter(StatusFilter.ACTIVE);
        controller.onPriorityFilter(Priority.HIGH);
        controller.onSort(SortOption.TITLE, true);
        controller.setInfoMessage("Some message");

        assertNotEquals(TaskQuery.defaultQuery(), controller.getCurrentState().query());

        controller.onClearFilters();

        assertEquals(TaskQuery.defaultQuery(), controller.getCurrentState().query());
        assertNull(controller.getCurrentState().infoMessage());
        assertNull(controller.getCurrentState().errorMessage());
    }

    @Test
    @DisplayName("Filter combinations combine category, status, priority, and search")
    void testFilterCombinations() {
        Category work = categoryService.create("Work", "#1976D2");
        Category personal = categoryService.create("Personal", "#388E3C");

        Task t1 = taskService.create(TaskDraft.ofSimple("Work report", "Q4 data", Priority.HIGH, work.getId()));
        Task t2 = taskService.create(TaskDraft.ofSimple("Work email", "Inbox check", Priority.LOW, work.getId()));
        Task t3 = taskService.create(TaskDraft.ofSimple("Personal report", "Budget", Priority.HIGH, personal.getId()));
        taskService.toggleComplete(t2.getId());

        receivedTasks.clear();

        controller.onSelectCategory(work.getId());
        controller.onStatusFilter(StatusFilter.ACTIVE);
        controller.onPriorityFilter(Priority.HIGH);
        controller.onSearch("report");

        List<Task> finalTasks = receivedTasks.get(receivedTasks.size() - 1);
        assertEquals(1, finalTasks.size());
        assertEquals(t1.getId(), finalTasks.get(0).getId());
    }
}
