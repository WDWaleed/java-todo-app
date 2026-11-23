package com.todoapp;

import com.todoapp.model.Category;
import com.todoapp.model.DeadlineTask;
import com.todoapp.model.Priority;
import com.todoapp.model.SimpleTask;
import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Developer helper populating sample categories and tasks for manual testing and demonstration.
 * Seeding executes strictly when the target task repository is empty.
 */
public final class DevSeed {

    private DevSeed() {
        // Prevent instantiation
    }

    /**
     * Seeds sample categories and tasks into the repositories only if the task repository contains 0 tasks.
     *
     * @param taskRepo     non-null task repository
     * @param categoryRepo non-null category repository
     * @return true if seeded, false if repository was already populated
     */
    public static boolean seedIfEmpty(TaskRepository taskRepo, CategoryRepository categoryRepo) {
        Objects.requireNonNull(taskRepo, "taskRepo must not be null");
        Objects.requireNonNull(categoryRepo, "categoryRepo must not be null");

        if (taskRepo.count() > 0) {
            return false;
        }

        Category work = new Category(UUID.randomUUID(), "Work", "#1976D2");
        Category study = new Category(UUID.randomUUID(), "Study", "#FB8C00");
        Category personal = new Category(UUID.randomUUID(), "Personal", "#388E3C");

        categoryRepo.save(work);
        categoryRepo.save(study);
        categoryRepo.save(personal);

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. Pinned high-priority deadline
        DeadlineTask t1 = new DeadlineTask(
                UUID.randomUUID(),
                "Finalize Software Construction Report",
                Priority.HIGH,
                study.getId(),
                false,
                true,
                now.minusDays(2),
                now.minusHours(1),
                today.plusDays(4)
        );

        // 2. Overdue deadline task
        DeadlineTask t2 = new DeadlineTask(
                UUID.randomUUID(),
                "Submit Lab 6 Reflection",
                Priority.HIGH,
                study.getId(),
                false,
                false,
                now.minusDays(5),
                now.minusDays(3),
                today.minusDays(1)
        );

        // 3. Completed deadline task
        DeadlineTask t3 = new DeadlineTask(
                UUID.randomUUID(),
                "Quarterly Tax Filing",
                Priority.MEDIUM,
                personal.getId(),
                true,
                false,
                now.minusDays(10),
                now.minusDays(2),
                today.minusDays(2)
        );

        // 4. Simple task - High priority Work
        SimpleTask t4 = new SimpleTask(
                UUID.randomUUID(),
                "Review Pull Request #42",
                Priority.HIGH,
                work.getId(),
                false,
                false,
                now.minusHours(4),
                now.minusHours(4)
        );

        // 5. Simple task - Medium priority Study
        SimpleTask t5 = new SimpleTask(
                UUID.randomUUID(),
                "Read JavaFX Scene Graph documentation",
                Priority.MEDIUM,
                study.getId(),
                false,
                false,
                now.minusDays(1),
                now.minusDays(1)
        );

        // 6. Simple task - Low priority Personal
        SimpleTask t6 = new SimpleTask(
                UUID.randomUUID(),
                "Book Dentist Appointment",
                Priority.LOW,
                personal.getId(),
                false,
                false,
                now.minusDays(4),
                now.minusDays(4)
        );

        // 7. Simple task - Completed
        SimpleTask t7 = new SimpleTask(
                UUID.randomUUID(),
                "Team Standup Meeting",
                Priority.MEDIUM,
                work.getId(),
                true,
                false,
                now.minusDays(2),
                now.minusDays(1)
        );

        // 8. Simple task - General category
        SimpleTask t8 = new SimpleTask(
                UUID.randomUUID(),
                "Renew Cloud Storage Subscription",
                Priority.LOW,
                Category.DEFAULT_ID,
                false,
                false,
                now.minusDays(3),
                now.minusDays(3)
        );

        List.of(t1, t2, t3, t4, t5, t6, t7, t8).forEach(taskRepo::save);

        return true;
    }
}
