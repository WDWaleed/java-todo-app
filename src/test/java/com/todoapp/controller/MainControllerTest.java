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
}
