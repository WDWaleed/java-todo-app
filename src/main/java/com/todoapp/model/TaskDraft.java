package com.todoapp.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Immutable value object representing a validated draft for creating or updating a task.
 *
 * @param type           the polymorphic task type (Simple, Checklist, Deadline)
 * @param title          task title (1–100 chars)
 * @param description    task description (≤2000 chars)
 * @param priority       urgency priority level
 * @param categoryId     category UUID reference
 * @param dueDate        calendar deadline date (required for Deadline tasks)
 * @param checklistItems list of sub-item texts (for Checklist tasks)
 */
public record TaskDraft(
        TaskType type,
        String title,
        String description,
        Priority priority,
        UUID categoryId,
        LocalDate dueDate,
        List<String> checklistItems
) {

    /**
     * Compact constructor normalizing collections and defaults.
     */
    public TaskDraft {
        description = description != null ? description : "";
        priority = priority != null ? priority : Priority.MEDIUM;
        categoryId = categoryId != null ? categoryId : Category.DEFAULT_ID;
        checklistItems = checklistItems != null ? List.copyOf(checklistItems) : List.of();
    }

    /**
     * Convenience builder for a simple task draft.
     *
     * @param title       title
     * @param description description
     * @param priority    priority
     * @param categoryId  category UUID
     * @return simple task draft
     */
    public static TaskDraft ofSimple(String title, String description, Priority priority, UUID categoryId) {
        return new TaskDraft(TaskType.SIMPLE, title, description, priority, categoryId, null, List.of());
    }

    /**
     * Convenience builder for a deadline task draft.
     *
     * @param title       title
     * @param description description
     * @param priority    priority
     * @param categoryId  category UUID
     * @param dueDate     due date
     * @return deadline task draft
     */
    public static TaskDraft ofDeadline(String title, String description, Priority priority, UUID categoryId, LocalDate dueDate) {
        return new TaskDraft(TaskType.DEADLINE, title, description, priority, categoryId, dueDate, List.of());
    }

    /**
     * Convenience builder for a checklist task draft.
     *
     * @param title          title
     * @param description    description
     * @param priority       priority
     * @param categoryId     category UUID
     * @param checklistItems sub-item text strings
     * @return checklist task draft
     */
    public static TaskDraft ofChecklist(String title, String description, Priority priority, UUID categoryId, List<String> checklistItems) {
        return new TaskDraft(TaskType.CHECKLIST, title, description, priority, categoryId, null, checklistItems);
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
        if (description != null && description.trim().length() > Task.MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Task description must not exceed " + Task.MAX_DESCRIPTION_LENGTH + " characters");
        }
        if (type == TaskType.DEADLINE && dueDate == null) {
            throw new IllegalArgumentException("Due date is required for deadline tasks");
        }
        if (type == TaskType.CHECKLIST && checklistItems != null) {
            for (String itemText : checklistItems) {
                if (itemText == null || itemText.trim().isEmpty()) {
                    throw new IllegalArgumentException("Checklist item text must not be empty");
                }
                if (itemText.trim().length() > ChecklistItem.MAX_TEXT_LENGTH) {
                    throw new IllegalArgumentException("Checklist item text must not exceed " + ChecklistItem.MAX_TEXT_LENGTH + " characters");
                }
            }
        }
    }
}
