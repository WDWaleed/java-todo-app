package com.todoapp.repository;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering InMemoryTaskRepository and InMemoryCategoryRepository.
 */
class RepositoryTest {

    private InMemoryTaskRepository taskRepo;
    private InMemoryCategoryRepository categoryRepo;

    @BeforeEach
    void setUp() {
        taskRepo = new InMemoryTaskRepository();
        categoryRepo = new InMemoryCategoryRepository();
    }

    // =========================================================================
    // InMemoryTaskRepository Tests
    // =========================================================================

    @Test
    @DisplayName("TaskRepository save, findById, count, insertion order, replace and delete")
    void testTaskRepositoryOperations() {
        assertEquals(0, taskRepo.count());
        assertTrue(taskRepo.findAll().isEmpty());

        Task task1 = new SimpleTask("Task 1", Priority.HIGH, Category.DEFAULT_ID);
        Task task2 = new SimpleTask("Task 2", Priority.LOW, Category.DEFAULT_ID);

        taskRepo.save(task1);
        taskRepo.save(task2);

        assertEquals(2, taskRepo.count());
        assertEquals(Optional.of(task1), taskRepo.findById(task1.getId()));
        assertEquals(Optional.of(task2), taskRepo.findById(task2.getId()));
        assertEquals(Optional.empty(), taskRepo.findById(UUID.randomUUID()));
        assertEquals(Optional.empty(), taskRepo.findById(null));

        // Insertion order preserved
        List<Task> all = taskRepo.findAll();
        assertEquals(task1.getId(), all.get(0).getId());
        assertEquals(task2.getId(), all.get(1).getId());

        // Replace task with same ID
        task1.setTitle("Task 1 Updated");
        taskRepo.save(task1);
        assertEquals(2, taskRepo.count());
        assertEquals("Task 1 Updated", taskRepo.findById(task1.getId()).orElseThrow().getTitle());

        // Delete by ID
        Optional<Task> deleted = taskRepo.deleteById(task1.getId());
        assertTrue(deleted.isPresent());
        assertEquals(task1.getId(), deleted.get().getId());
        assertEquals(1, taskRepo.count());
        assertEquals(Optional.empty(), taskRepo.findById(task1.getId()));

        assertFalse(taskRepo.deleteById(UUID.randomUUID()).isPresent());
        assertFalse(taskRepo.deleteById(null).isPresent());

        // Clear
        taskRepo.clear();
        assertEquals(0, taskRepo.count());
        assertTrue(taskRepo.findAll().isEmpty());

        assertThrows(NullPointerException.class, () -> taskRepo.save(null));
    }

    // =========================================================================
    // InMemoryCategoryRepository Tests
    // =========================================================================

    @Test
    @DisplayName("CategoryRepository default category, search, and delete restrictions")
    void testCategoryRepositoryOperations() {
        // Default category is guaranteed to be present initially
        assertEquals(1, categoryRepo.count());
        Optional<Category> defaultCat = categoryRepo.findById(Category.DEFAULT_ID);
        assertTrue(defaultCat.isPresent());
        assertEquals("General", defaultCat.get().getName());

        // Save custom category
        Category custom = new Category("Work", "#1976D2");
        categoryRepo.save(custom);
        assertEquals(2, categoryRepo.count());

        // findByNameIgnoreCase
        assertTrue(categoryRepo.findByNameIgnoreCase("general").isPresent());
        assertTrue(categoryRepo.findByNameIgnoreCase("GENERAL").isPresent());
        assertTrue(categoryRepo.findByNameIgnoreCase("work").isPresent());
        assertTrue(categoryRepo.findByNameIgnoreCase(" WORK ").isPresent());
        assertFalse(categoryRepo.findByNameIgnoreCase("NonExistent").isPresent());
        assertFalse(categoryRepo.findByNameIgnoreCase(null).isPresent());
        assertFalse(categoryRepo.findByNameIgnoreCase("").isPresent());

        // Default category cannot be deleted
        Optional<Category> deletedDefault = categoryRepo.deleteById(Category.DEFAULT_ID);
        assertFalse(deletedDefault.isPresent());
        assertEquals(2, categoryRepo.count());

        // Custom category can be deleted
        Optional<Category> deletedCustom = categoryRepo.deleteById(custom.getId());
        assertTrue(deletedCustom.isPresent());
        assertEquals(custom.getId(), deletedCustom.get().getId());
        assertEquals(1, categoryRepo.count());

        // Clear resets custom categories but preserves default category
        categoryRepo.save(new Category("Personal", "#388E3C"));
        assertEquals(2, categoryRepo.count());
        categoryRepo.clear();
        assertEquals(1, categoryRepo.count());
        assertTrue(categoryRepo.findById(Category.DEFAULT_ID).isPresent());

        assertFalse(categoryRepo.findById(null).isPresent());
        assertFalse(categoryRepo.deleteById(null).isPresent());
        assertThrows(NullPointerException.class, () -> categoryRepo.save(null));
    }
}
