package com.todoapp;

import com.todoapp.model.Category;
import com.todoapp.model.ChecklistItem;
import com.todoapp.model.ChecklistTask;
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
                "Review architectural diagrams and ensure all test evidence is included.",
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
                "Upload assignment PDF to course portal.",
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
                "Submit receipts and tax declaration form.",
                Priority.MEDIUM,
                personal.getId(),
                true,
                false,
                now.minusDays(10),
                now.minusDays(2),
                today.minusDays(2)
        );

        // 4. Checklist task - in progress
        ChecklistTask t4 = new ChecklistTask(
                UUID.randomUUID(),
                "Grocery Shopping",
                "Weekly household restocking.",
                Priority.MEDIUM,
                personal.getId(),
                false,
                true,
                now.minusDays(1),
                now,
                List.of(
                        new ChecklistItem(UUID.randomUUID(), "Fresh milk", true),
                        new ChecklistItem(UUID.randomUUID(), "Whole wheat bread", true),
                        new ChecklistItem(UUID.randomUUID(), "Eggs (dozen)", false),
                        new ChecklistItem(UUID.randomUUID(), "Olive oil", false),
                        new ChecklistItem(UUID.randomUUID(), "Greek yogurt", false)
                )
        );

        // 5. Checklist task - fully done
        ChecklistTask t5 = new ChecklistTask(
                UUID.randomUUID(),
                "Sprint Planning Checklist",
                "Standard agile team preparations.",
                Priority.HIGH,
                work.getId(),
                true,
                false,
                now.minusDays(3),
                now.minusDays(1),
                List.of(
                        new ChecklistItem(UUID.randomUUID(), "Groom backlog", true),
                        new ChecklistItem(UUID.randomUUID(), "Estimate story points", true),
                        new ChecklistItem(UUID.randomUUID(), "Confirm team capacity", true)
                )
        );

        // 6. Checklist task - all active
        ChecklistTask t6 = new ChecklistTask(
                UUID.randomUUID(),
                "Home Office Setup",
                "Ergonomic upgrades for workstation.",
                Priority.LOW,
                personal.getId(),
                false,
                false,
                now.minusHours(12),
                now.minusHours(12),
                List.of(
                        new ChecklistItem(UUID.randomUUID(), "Monitor light bar", false),
                        new ChecklistItem(UUID.randomUUID(), "Cable management tray", false),
                        new ChecklistItem(UUID.randomUUID(), "Footrest cushion", false)
                )
        );

        // 7. Checklist task - General category
        ChecklistTask t7 = new ChecklistTask(
                UUID.randomUUID(),
                "Pack for Weekend Trip",
                "Essentials for travel.",
                Priority.MEDIUM,
                Category.DEFAULT_ID,
                false,
                false,
                now.minusHours(6),
                now.minusHours(2),
                List.of(
                        new ChecklistItem(UUID.randomUUID(), "Passport & ID", true),
                        new ChecklistItem(UUID.randomUUID(), "Phone charger", true),
                        new ChecklistItem(UUID.randomUUID(), "Weather jacket", false)
                )
        );

        // 8. Simple task - High priority Work
        SimpleTask t8 = new SimpleTask(
                UUID.randomUUID(),
                "Review Pull Request #42",
                "Check error handling and thread safety on database connection pool changes.",
                Priority.HIGH,
                work.getId(),
                false,
                false,
                now.minusHours(4),
                now.minusHours(4)
        );

        // 9. Simple task - Medium priority Study
        SimpleTask t9 = new SimpleTask(
                UUID.randomUUID(),
                "Read JavaFX Scene Graph documentation",
                "Explore layout panes, CSS pseudo-classes, and observable collection binding rules.",
                Priority.MEDIUM,
                study.getId(),
                false,
                false,
                now.minusDays(1),
                now.minusDays(1)
        );

        // 10. Simple task - Low priority Personal
        SimpleTask t10 = new SimpleTask(
                UUID.randomUUID(),
                "Book Dentist Appointment",
                "Call clinic for regular 6-month routine checkup.",
                Priority.LOW,
                personal.getId(),
                false,
                false,
                now.minusDays(4),
                now.minusDays(4)
        );

        // 11. Simple task - Completed
        SimpleTask t11 = new SimpleTask(
                UUID.randomUUID(),
                "Team Standup Meeting",
                "Synced with engineering team on weekly sprint progress.",
                Priority.MEDIUM,
                work.getId(),
                true,
                false,
                now.minusDays(2),
                now.minusDays(1)
        );

        // 12. Simple task - General category
        SimpleTask t12 = new SimpleTask(
                UUID.randomUUID(),
                "Renew Cloud Storage Subscription",
                "Annual renewal for cloud backup storage service.",
                Priority.LOW,
                Category.DEFAULT_ID,
                false,
                false,
                now.minusDays(3),
                now.minusDays(3)
        );

        List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12).forEach(taskRepo::save);

        return true;
    }
}
