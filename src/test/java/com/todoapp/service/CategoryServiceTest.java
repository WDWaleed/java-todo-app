package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering CategoryService business logic, validation and task reassignment.
 */
class CategoryServiceTest {

    private InMemoryCategoryRepository categoryRepo;
    private InMemoryTaskRepository taskRepo;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepo = new InMemoryCategoryRepository();
        taskRepo = new InMemoryTaskRepository();
        categoryService = new CategoryService(categoryRepo, taskRepo);
    }

    @Test
    @DisplayName("Constructor null checks")
    void testConstructorValidation() {
        assertThrows(NullPointerException.class, () -> new CategoryService(null, taskRepo));
        assertThrows(NullPointerException.class, () -> new CategoryService(categoryRepo, null));
    }

    @Test
    @DisplayName("Create category: success, trimming, and uppercase color")
    void testCreateCategorySuccess() {
        AtomicInteger eventCount = new AtomicInteger();
        categoryService.addListener((type, id) -> {
            if (type == ChangeType.CATEGORY_CHANGED) {
                eventCount.incrementAndGet();
            }
        });

        Category cat = categoryService.create("  Work Projects  ", "#1976d2");
        assertNotNull(cat.getId());
        assertEquals("Work Projects", cat.getName());
        assertEquals("#1976D2", cat.getColorHex());
        assertEquals(1, eventCount.get());

        assertEquals(2, categoryService.getAll().size());
        assertEquals(cat, categoryService.get(cat.getId()));
    }

    @Test
    @DisplayName("Create category: validation failures for empty, overlength, duplicate name or invalid color")
    void testCreateCategoryValidations() {
        // Blank name
        ValidationException exBlank = assertThrows(ValidationException.class, () -> categoryService.create("", "#123456"));
        assertEquals("Category name is required", exBlank.getMessage());

        assertThrows(ValidationException.class, () -> categoryService.create("   ", "#123456"));
        assertThrows(ValidationException.class, () -> categoryService.create(null, "#123456"));

        // Over-length name
        assertThrows(ValidationException.class, () -> categoryService.create("a".repeat(31), "#123456"));

        // Duplicate name (case-insensitive)
        categoryService.create("Urgent", "#FF0000");
        ValidationException exDup = assertThrows(ValidationException.class, () -> categoryService.create("  urgent  ", "#00FF00"));
        assertTrue(exDup.getMessage().contains("already exists"));

        // Duplicate of default General category
        ValidationException exGen = assertThrows(ValidationException.class, () -> categoryService.create("general", "#000000"));
        assertTrue(exGen.getMessage().contains("already exists"));

        // Invalid color hex
        assertThrows(ValidationException.class, () -> categoryService.create("Valid Name", "invalid-color"));
        assertThrows(ValidationException.class, () -> categoryService.create("Valid Name", "#12345"));
        assertThrows(ValidationException.class, () -> categoryService.create("Valid Name", null));
    }

    @Test
    @DisplayName("Rename category: success, default category protection, and uniqueness")
    void testRenameCategory() {
        Category cat = categoryService.create("Original", "#112233");

        Category renamed = categoryService.rename(cat.getId(), "Renamed");
        assertEquals("Renamed", renamed.getName());
        assertEquals("Renamed", categoryService.get(cat.getId()).getName());

        // Default category cannot be renamed
        ValidationException exDefault = assertThrows(ValidationException.class,
                () -> categoryService.rename(Category.DEFAULT_ID, "New Default"));
        assertEquals("The default 'General' category cannot be renamed", exDefault.getMessage());

        // Duplicate name check
        Category other = categoryService.create("Other", "#445566");
        assertThrows(ValidationException.class, () -> categoryService.rename(cat.getId(), "other"));

        // Non-existent ID
        assertThrows(NotFoundException.class, () -> categoryService.rename(UUID.randomUUID(), "NonExistent"));
    }

    @Test
    @DisplayName("Recolor category: success and invalid color handling")
    void testRecolorCategory() {
        Category cat = categoryService.create("Design", "#112233");
        Category recolored = categoryService.recolor(cat.getId(), "#AABBCC");
        assertEquals("#AABBCC", recolored.getColorHex());

        // Can recolor default category
        Category defaultRecolored = categoryService.recolor(Category.DEFAULT_ID, "#222222");
        assertEquals("#222222", defaultRecolored.getColorHex());

        assertThrows(ValidationException.class, () -> categoryService.recolor(cat.getId(), "invalid"));
        assertThrows(NotFoundException.class, () -> categoryService.recolor(UUID.randomUUID(), "#123456"));
    }

    @Test
    @DisplayName("Delete category: default category protection, and task reassignment to General")
    void testDeleteCategoryAndTaskReassignment() {
        // Default category cannot be deleted
        ValidationException exDefault = assertThrows(ValidationException.class,
                () -> categoryService.delete(Category.DEFAULT_ID));
        assertEquals("The default 'General' category cannot be deleted", exDefault.getMessage());

        Category work = categoryService.create("Work", "#1976D2");
        Category home = categoryService.create("Home", "#388E3C");

        // Create tasks assigned to categories
        Task task1 = new SimpleTask("Task 1", "Work task 1", Priority.HIGH, work.getId());
        Task task2 = new SimpleTask("Task 2", "Work task 2", Priority.MEDIUM, work.getId());
        Task task3 = new SimpleTask("Task 3", "Home task", Priority.LOW, home.getId());

        taskRepo.save(task1);
        taskRepo.save(task2);
        taskRepo.save(task3);

        assertEquals(2, categoryService.countTasks(work.getId()));
        assertEquals(1, categoryService.countTasks(home.getId()));
        assertEquals(0, categoryService.countTasks(Category.DEFAULT_ID));

        // Delete Work category
        Category deleted = categoryService.delete(work.getId());
        assertEquals("Work", deleted.getName());
        assertThrows(NotFoundException.class, () -> categoryService.get(work.getId()));

        // Tasks previously in Work are now reassigned to General
        assertEquals(2, categoryService.countTasks(Category.DEFAULT_ID));
        assertEquals(1, categoryService.countTasks(home.getId()));

        Task updatedTask1 = taskRepo.findById(task1.getId()).orElseThrow();
        Task updatedTask2 = taskRepo.findById(task2.getId()).orElseThrow();
        assertEquals(Category.DEFAULT_ID, updatedTask1.getCategoryId());
        assertEquals(Category.DEFAULT_ID, updatedTask2.getCategoryId());

        // Deleting non-existent category
        assertThrows(NotFoundException.class, () -> categoryService.delete(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Get and count helper methods")
    void testGetAndCountHelpers() {
        assertEquals(0, categoryService.countTasks(null));
        assertThrows(NotFoundException.class, () -> categoryService.get(null));
        assertThrows(NotFoundException.class, () -> categoryService.get(UUID.randomUUID()));

        List<Category> all = categoryService.getAll();
        assertEquals(1, all.size());
        assertEquals("General", all.get(0).getName());
    }

    @Test
    @DisplayName("Listener exceptions are isolated and do not break other listeners")
    void testListenerExceptionIsolation() {
        AtomicInteger successCount = new AtomicInteger();

        categoryService.addListener((type, id) -> {
            throw new RuntimeException("Simulated listener failure");
        });
        categoryService.addListener((type, id) -> successCount.incrementAndGet());

        assertDoesNotThrow(() -> categoryService.create("SafeCategory", "#123456"));
        assertEquals(1, successCount.get());
    }
}
