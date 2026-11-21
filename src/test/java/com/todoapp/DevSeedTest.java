package com.todoapp;

import com.todoapp.model.Category;
import com.todoapp.model.ChecklistTask;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.model.Task;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying developer sample data seeding functionality.
 */
class DevSeedTest {

    @Test
    @DisplayName("seedIfEmpty populates sample tasks across all types, priorities, and categories")
    void testSeedIfEmptySuccess() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        boolean seeded = DevSeed.seedIfEmpty(taskRepo, categoryRepo);
        assertTrue(seeded, "Expected seeding to succeed when repository is empty");

        assertEquals(12, taskRepo.count(), "Expected exactly 12 sample tasks");
        assertTrue(categoryRepo.count() >= 4, "Expected at least 4 categories (General, Work, Study, Personal)");

        List<Task> tasks = taskRepo.findAll();

        // Check types
        assertTrue(tasks.stream().anyMatch(t -> t instanceof SimpleTask), "Expected SimpleTask in seed");
        assertTrue(tasks.stream().anyMatch(t -> t instanceof ChecklistTask), "Expected ChecklistTask in seed");
        assertTrue(tasks.stream().anyMatch(t -> t instanceof DeadlineTask), "Expected DeadlineTask in seed");

        // Check priorities
        assertTrue(tasks.stream().anyMatch(t -> t.getPriority() == Priority.HIGH), "Expected HIGH priority in seed");
        assertTrue(tasks.stream().anyMatch(t -> t.getPriority() == Priority.MEDIUM), "Expected MEDIUM priority in seed");
        assertTrue(tasks.stream().anyMatch(t -> t.getPriority() == Priority.LOW), "Expected LOW priority in seed");

        // Check pinned and completed
        assertTrue(tasks.stream().anyMatch(Task::isPinned), "Expected pinned task in seed");
        assertTrue(tasks.stream().anyMatch(Task::isCompleted), "Expected completed task in seed");
    }

    @Test
    @DisplayName("seedIfEmpty does nothing when task repository is not empty")
    void testSeedIfEmptySkippedWhenPopulated() {
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        taskRepo.save(new SimpleTask("Pre-existing Task", "", Priority.LOW, Category.DEFAULT_ID));

        boolean seeded = DevSeed.seedIfEmpty(taskRepo, categoryRepo);
        assertFalse(seeded, "Expected seeding to be skipped when tasks already exist");
        assertEquals(1, taskRepo.count());
    }
}
