package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link TaskService#query(TaskQuery)} engine,
 * covering composed filtering, live keyword search, sorting strategies,
 * the pinned-first invariant, and query performance.
 */
class TaskQueryEngineTest {

    private InMemoryTaskRepository taskRepo;
    private InMemoryCategoryRepository categoryRepo;
    private TaskService taskService;
    private Category catWork;
    private Category catPersonal;

    @BeforeEach
    void setUp() {
        taskRepo = new InMemoryTaskRepository();
        categoryRepo = new InMemoryCategoryRepository();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-20T10:00:00.00Z"), ZoneId.of("UTC"));
        taskService = new TaskService(taskRepo, categoryRepo, fixedClock);

        catWork = new Category("Work", "#1976D2");
        catPersonal = new Category("Personal", "#388E3C");
        categoryRepo.save(catWork);
        categoryRepo.save(catPersonal);
    }

    // =========================================================================
    // 1. TaskQuery Record & Defaults Tests
    // =========================================================================

    @Test
    @DisplayName("TaskQuery default values and with-helpers")
    void testTaskQueryDefaultsAndWithHelpers() {
        TaskQuery query = TaskQuery.defaultQuery();
        assertNull(query.categoryId());
        assertEquals(StatusFilter.ALL, query.status());
        assertNull(query.priority());
        assertEquals("", query.searchText());
        assertEquals(SortOption.CREATED, query.sort());
        assertFalse(query.ascending());

        UUID catId = UUID.randomUUID();
        TaskQuery updated = query.withCategory(catId)
                .withStatus(StatusFilter.ACTIVE)
                .withPriority(Priority.HIGH)
                .withSearch("meeting")
                .withSort(SortOption.TITLE, true);

        assertEquals(catId, updated.categoryId());
        assertEquals(StatusFilter.ACTIVE, updated.status());
        assertEquals(Priority.HIGH, updated.priority());
        assertEquals("meeting", updated.searchText());
        assertEquals(SortOption.TITLE, updated.sort());
        assertTrue(updated.ascending());
    }

    @Test
    @DisplayName("SortOption labels and comparators")
    void testSortOptionProperties() {
        assertEquals("Priority", SortOption.PRIORITY.getLabel());
        assertEquals("Due Date", SortOption.DUE_DATE.getLabel());
        assertEquals("Date Created", SortOption.CREATED.getLabel());
        assertEquals("Title", SortOption.TITLE.getLabel());
        assertEquals("Priority", SortOption.PRIORITY.toString());

        assertNotNull(SortOption.PRIORITY.getComparator(true));
        assertNotNull(SortOption.PRIORITY.getComparator(false));
    }

    // =========================================================================
    // 2. Filtering Tests
    // =========================================================================

    @Test
    @DisplayName("Filter by category, status, and priority")
    void testFilteringCombinations() {
        Task t1 = taskService.create(TaskDraft.ofSimple("Work High Active", Priority.HIGH, catWork.getId()));
        Task t2 = taskService.create(TaskDraft.ofSimple("Work Med Complete", Priority.MEDIUM, catWork.getId()));
        taskService.toggleComplete(t2.getId());

        Task t3 = taskService.create(TaskDraft.ofSimple("Personal Low Active", Priority.LOW, catPersonal.getId()));
        Task t4 = taskService.create(TaskDraft.ofSimple("General High Active", Priority.HIGH, Category.DEFAULT_ID));

        // Category filter
        List<Task> workOnly = taskService.query(TaskQuery.defaultQuery().withCategory(catWork.getId()));
        assertEquals(2, workOnly.size());
        assertTrue(workOnly.stream().allMatch(t -> t.getCategoryId().equals(catWork.getId())));

        // Status filter: ACTIVE
        List<Task> activeOnly = taskService.query(TaskQuery.defaultQuery().withStatus(StatusFilter.ACTIVE));
        assertEquals(3, activeOnly.size());
        assertTrue(activeOnly.stream().noneMatch(Task::isCompleted));

        // Status filter: COMPLETED
        List<Task> completedOnly = taskService.query(TaskQuery.defaultQuery().withStatus(StatusFilter.COMPLETED));
        assertEquals(1, completedOnly.size());
        assertEquals(t2.getId(), completedOnly.get(0).getId());

        // Priority filter: HIGH
        List<Task> highOnly = taskService.query(TaskQuery.defaultQuery().withPriority(Priority.HIGH));
        assertEquals(2, highOnly.size());
        assertTrue(highOnly.stream().allMatch(t -> t.getPriority() == Priority.HIGH));

        // Combined: Work + Active + High
        List<Task> combined = taskService.query(TaskQuery.defaultQuery()
                .withCategory(catWork.getId())
                .withStatus(StatusFilter.ACTIVE)
                .withPriority(Priority.HIGH));
        assertEquals(1, combined.size());
        assertEquals(t1.getId(), combined.get(0).getId());
    }

    // =========================================================================
    // 3. Keyword Search Tests
    // =========================================================================

    @Test
    @DisplayName("Live search matches title case-insensitively")
    void testKeywordSearch() {
        taskService.create(TaskDraft.ofSimple("Learn JavaFX Controls", Priority.HIGH, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Write JUnit Tests", Priority.MEDIUM, Category.DEFAULT_ID));
        taskService.create(TaskDraft.ofSimple("Review Java Pull Request", Priority.LOW, Category.DEFAULT_ID));

        // Search title
        List<Task> searchTitle = taskService.query(TaskQuery.defaultQuery().withSearch("javafx"));
        assertEquals(1, searchTitle.size());
        assertEquals("Learn JavaFX Controls", searchTitle.get(0).getTitle());

        // Match multiple in title
        List<Task> searchJava = taskService.query(TaskQuery.defaultQuery().withSearch("java"));
        assertEquals(2, searchJava.size());

        // Whitespace and case insensitivity
        List<Task> searchTrim = taskService.query(TaskQuery.defaultQuery().withSearch("  JUNIT  "));
        assertEquals(1, searchTrim.size());

        // No match returns empty list
        List<Task> noMatch = taskService.query(TaskQuery.defaultQuery().withSearch("NonExistentTerm"));
        assertTrue(noMatch.isEmpty());

        // Null query defaults cleanly
        assertDoesNotThrow(() -> taskService.query(null));
    }

    // =========================================================================
    // 4. Sorting Strategies Tests
    // =========================================================================

    @Test
    @DisplayName("Sort by Priority ascending (HIGH first) and descending (LOW first)")
    void testSortByPriority() {
        Task low = taskService.create(TaskDraft.ofSimple("Low Task", Priority.LOW, Category.DEFAULT_ID));
        Task med = taskService.create(TaskDraft.ofSimple("Med Task", Priority.MEDIUM, Category.DEFAULT_ID));
        Task high = taskService.create(TaskDraft.ofSimple("High Task", Priority.HIGH, Category.DEFAULT_ID));

        // Ascending = Rank 1 (High) first
        List<Task> asc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.PRIORITY, true));
        assertEquals(high.getId(), asc.get(0).getId());
        assertEquals(med.getId(), asc.get(1).getId());
        assertEquals(low.getId(), asc.get(2).getId());

        // Descending = Rank 3 (Low) first
        List<Task> desc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.PRIORITY, false));
        assertEquals(low.getId(), desc.get(0).getId());
        assertEquals(med.getId(), desc.get(1).getId());
        assertEquals(high.getId(), desc.get(2).getId());
    }

    @Test
    @DisplayName("Sort by Due Date: tasks without due date always appear last in both directions")
    void testSortByDueDate() {
        LocalDate d1 = LocalDate.of(2026, 11, 20);
        LocalDate d2 = LocalDate.of(2026, 11, 25);
        LocalDate d3 = LocalDate.of(2026, 12, 1);

        Task noDate = taskService.create(TaskDraft.ofSimple("No Date Task", Priority.MEDIUM, Category.DEFAULT_ID));
        Task early = taskService.create(TaskDraft.ofDeadline("Early Task", Priority.MEDIUM, Category.DEFAULT_ID, d1));
        Task mid = taskService.create(TaskDraft.ofDeadline("Mid Task", Priority.MEDIUM, Category.DEFAULT_ID, d2));
        Task late = taskService.create(TaskDraft.ofDeadline("Late Task", Priority.MEDIUM, Category.DEFAULT_ID, d3));

        // Ascending: Earliest first, null dates last
        List<Task> asc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.DUE_DATE, true));
        assertEquals(early.getId(), asc.get(0).getId());
        assertEquals(mid.getId(), asc.get(1).getId());
        assertEquals(late.getId(), asc.get(2).getId());
        assertEquals(noDate.getId(), asc.get(3).getId());

        // Descending: Latest first, null dates STILL last
        List<Task> desc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.DUE_DATE, false));
        assertEquals(late.getId(), desc.get(0).getId());
        assertEquals(mid.getId(), desc.get(1).getId());
        assertEquals(early.getId(), desc.get(2).getId());
        assertEquals(noDate.getId(), desc.get(3).getId());
    }

    @Test
    @DisplayName("Sort by Title case-insensitively")
    void testSortByTitle() {
        Task b = taskService.create(TaskDraft.ofSimple("banana", Priority.MEDIUM, Category.DEFAULT_ID));
        Task a = taskService.create(TaskDraft.ofSimple("Apple", Priority.MEDIUM, Category.DEFAULT_ID));
        Task c = taskService.create(TaskDraft.ofSimple("cherry", Priority.MEDIUM, Category.DEFAULT_ID));

        List<Task> asc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.TITLE, true));
        assertEquals(a.getId(), asc.get(0).getId());
        assertEquals(b.getId(), asc.get(1).getId());
        assertEquals(c.getId(), asc.get(2).getId());

        List<Task> desc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.TITLE, false));
        assertEquals(c.getId(), desc.get(0).getId());
        assertEquals(b.getId(), desc.get(1).getId());
        assertEquals(a.getId(), desc.get(2).getId());
    }

    @Test
    @DisplayName("Sort by Creation Date: oldest first on ascending, newest first on descending")
    void testSortByCreated() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 20, 10, 0);

        SimpleTask t1 = new SimpleTask(UUID.randomUUID(), "T1", Priority.MEDIUM, Category.DEFAULT_ID, false, false, baseTime, baseTime);
        SimpleTask t2 = new SimpleTask(UUID.randomUUID(), "T2", Priority.MEDIUM, Category.DEFAULT_ID, false, false, baseTime.plusHours(1), baseTime.plusHours(1));
        SimpleTask t3 = new SimpleTask(UUID.randomUUID(), "T3", Priority.MEDIUM, Category.DEFAULT_ID, false, false, baseTime.plusHours(2), baseTime.plusHours(2));

        taskRepo.save(t1);
        taskRepo.save(t2);
        taskRepo.save(t3);

        List<Task> asc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.CREATED, true));
        assertEquals(t1.getId(), asc.get(0).getId());
        assertEquals(t2.getId(), asc.get(1).getId());
        assertEquals(t3.getId(), asc.get(2).getId());

        List<Task> desc = taskService.query(TaskQuery.defaultQuery().withSort(SortOption.CREATED, false));
        assertEquals(t3.getId(), desc.get(0).getId());
        assertEquals(t2.getId(), desc.get(1).getId());
        assertEquals(t1.getId(), desc.get(2).getId());
    }

    // =========================================================================
    // 5. Pinned-First Invariant Tests
    // =========================================================================

    @ParameterizedTest
    @EnumSource(SortOption.class)
    @DisplayName("Pinned tasks always appear first regardless of sort option and direction")
    void testPinnedFirstInvariant(SortOption option) {
        LocalDate date = LocalDate.of(2026, 11, 20);

        Task unpinnedZ = taskService.create(TaskDraft.ofDeadline("Zebra", Priority.HIGH, Category.DEFAULT_ID, date));
        Task pinnedA = taskService.create(TaskDraft.ofDeadline("Ant", Priority.LOW, Category.DEFAULT_ID, date.plusDays(10)));
        taskService.togglePinned(pinnedA.getId());

        // Test Ascending
        List<Task> asc = taskService.query(TaskQuery.defaultQuery().withSort(option, true));
        assertTrue(asc.get(0).isPinned(), "First task must be pinned for " + option + " asc");
        assertEquals(pinnedA.getId(), asc.get(0).getId());

        // Test Descending
        List<Task> desc = taskService.query(TaskQuery.defaultQuery().withSort(option, false));
        assertTrue(desc.get(0).isPinned(), "First task must be pinned for " + option + " desc");
        assertEquals(pinnedA.getId(), desc.get(0).getId());
    }

    // =========================================================================
    // 6. Count Helpers Tests
    // =========================================================================

    @Test
    @DisplayName("countByPriority, countActive, and countCompleted")
    void testCountHelpers() {
        Task t1 = taskService.create(TaskDraft.ofSimple("H1", Priority.HIGH, catWork.getId()));
        Task t2 = taskService.create(TaskDraft.ofSimple("H2", Priority.HIGH, catWork.getId()));
        Task t3 = taskService.create(TaskDraft.ofSimple("M1", Priority.MEDIUM, catWork.getId()));
        Task t4 = taskService.create(TaskDraft.ofSimple("L1", Priority.LOW, catPersonal.getId()));

        taskService.toggleComplete(t1.getId());

        assertEquals(3, taskService.countActive());
        assertEquals(1, taskService.countCompleted());

        // Count by priority under work category
        Map<Priority, Integer> workCounts = taskService.countByPriority(TaskQuery.defaultQuery().withCategory(catWork.getId()));
        assertEquals(2, workCounts.get(Priority.HIGH));
        assertEquals(1, workCounts.get(Priority.MEDIUM));
        assertEquals(0, workCounts.get(Priority.LOW));

        // Null query defaults cleanly
        Map<Priority, Integer> allCounts = taskService.countByPriority(null);
        assertEquals(2, allCounts.get(Priority.HIGH));
        assertEquals(1, allCounts.get(Priority.MEDIUM));
        assertEquals(1, allCounts.get(Priority.LOW));
    }

    // =========================================================================
    // 7. Performance Sanity Test
    // =========================================================================

    @Test
    @Tag("performance")
    @DisplayName("Performance sanity test: query on 1,000 tasks executes in < 200ms")
    void testQueryPerformance1000Tasks() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 20, 12, 0);
        Priority[] priorities = Priority.values();

        for (int i = 0; i < 1000; i++) {
            boolean pinned = (i % 20 == 0);
            boolean completed = (i % 3 == 0);
            Priority p = priorities[i % priorities.length];
            UUID cat = (i % 2 == 0) ? catWork.getId() : catPersonal.getId();

            SimpleTask task = new SimpleTask(
                    UUID.randomUUID(),
                    "Task Number " + i + " benchmark",
                    p,
                    cat,
                    completed,
                    pinned,
                    baseTime.plusMinutes(i),
                    baseTime.plusMinutes(i)
            );
            taskRepo.save(task);
        }

        assertEquals(1000, taskRepo.count());

        TaskQuery query = TaskQuery.defaultQuery()
                .withStatus(StatusFilter.ACTIVE)
                .withSearch("benchmark")
                .withSort(SortOption.PRIORITY, true);

        long start = System.currentTimeMillis();
        List<Task> results = taskService.query(query);
        long elapsed = System.currentTimeMillis() - start;

        assertFalse(results.isEmpty());
        assertTrue(elapsed < 200, "Expected query < 200ms, took " + elapsed + "ms");
    }
}
