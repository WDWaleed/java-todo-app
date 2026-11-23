package com.todoapp.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Immutable value object representing a validated draft for creating or updating a task.
 *
 * @param type       the polymorphic task type (Simple, Deadline)
 * @param title      task title (1–100 chars)
 * @param priority   urgency priority level
 * @param categoryId category UUID reference
 * @param dueDate    calendar deadline date (required for Deadline tasks)
 */
public record TaskDraft(
        TaskType type,
        String title,
        Priority priority,
        UUID categoryId,
        LocalDate dueDate
) {

    /**
     * Compact constructor normalizing defaults.
     */
    public TaskDraft {
        priority = priority != null ? priority : Priority.MEDIUM;
        categoryId = categoryId != null ? categoryId : Category.DEFAULT_ID;
    }

    /**
     * Convenience builder for a simple task draft.
     *
     * @param title      title
     * @param priority   priority
     * @param categoryId category UUID
     * @return simple task draft
     */
    public static TaskDraft ofSimple(String title, Priority priority, UUID categoryId) {
        return new TaskDraft(TaskType.SIMPLE, title, priority, categoryId, null);
    }

    /**
     * Convenience builder for a deadline task draft.
     *
     * @param title      title
     * @param priority   priority
     * @param categoryId category UUID
     * @param dueDate    due date
     * @return deadline task draft
     */
    public static TaskDraft ofDeadline(String title, Priority priority, UUID categoryId, LocalDate dueDate) {
        return new TaskDraft(TaskType.DEADLINE, title, priority, categoryId, dueDate);
    }

    /**
     * Validates the draft against domain model invariants.
     *
     * @throws IllegalArgumentException if any field constraint is violated
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("Task type must not be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title must not be empty");
        }
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() > Task.MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Task title must not exceed " + Task.MAX_TITLE_LENGTH + " characters");
        }
        if (type == TaskType.DEADLINE && dueDate == null) {
            throw new IllegalArgumentException("Due date is required for deadline tasks");
        }
    }
}
