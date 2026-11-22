package com.todoapp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Factory class providing static creation and in-place mutation methods
 * for polymorphic {@link Task} entities based on {@link TaskDraft} instances.
 */
public final class TaskFactory {

    private TaskFactory() {
        // Prevent instantiation of utility factory class
    }

    /**
     * Creates a new concrete {@link Task} subtype corresponding to the given draft.
     * Uses current system time for creation and modification timestamps.
     *
     * @param draft validated task draft
     * @return concrete Task instance (SimpleTask, ChecklistTask, or DeadlineTask)
     * @throws IllegalArgumentException if draft is null or fails validation
     */
    public static Task create(TaskDraft draft) {
        return create(draft, java.time.LocalDateTime.now());
    }

    /**
     * Creates a new concrete {@link Task} subtype corresponding to the given draft
     * with an explicit timestamp for deterministic testing and clock injection.
     *
     * @param draft     validated task draft
     * @param timestamp non-null timestamp for createdAt and updatedAt
     * @return concrete Task instance
     * @throws IllegalArgumentException if draft is null or fails validation
     */
    public static Task create(TaskDraft draft, java.time.LocalDateTime timestamp) {
        Objects.requireNonNull(draft, "Task draft must not be null");
        draft.validate();
        java.time.LocalDateTime now = timestamp != null ? timestamp : java.time.LocalDateTime.now();
        UUID id = UUID.randomUUID();

        return switch (draft.type()) {
            case SIMPLE -> new SimpleTask(
                    id,
                    draft.title(),
                    draft.description(),
                    draft.priority(),
                    draft.categoryId(),
                    false,
                    false,
                    now,
                    now
            );
            case CHECKLIST -> {
                List<ChecklistItem> items = new ArrayList<>();
                for (String text : draft.checklistItems()) {
                    items.add(new ChecklistItem(text));
                }
                yield new ChecklistTask(
                        id,
                        draft.title(),
                        draft.description(),
                        draft.priority(),
                        draft.categoryId(),
                        false,
                        false,
                        now,
                        now,
                        items
                );
            }
            case DEADLINE -> new DeadlineTask(
                    id,
                    draft.title(),
                    draft.description(),
                    draft.priority(),
                    draft.categoryId(),
                    false,
                    false,
                    now,
                    now,
                    draft.dueDate()
            );
        };
    }

    /**
     * Updates an existing task in-place using values from a task draft.
     * Note: Changing a task's polymorphic {@link TaskType} is prohibited in v1.0.
     *
     * @param existing target task to modify
     * @param draft    validated task draft containing updated attributes
     * @throws IllegalArgumentException if draft is null, validation fails, or type differs
     */
    public static void apply(Task existing, TaskDraft draft) {
        Objects.requireNonNull(existing, "Target task must not be null");
        Objects.requireNonNull(draft, "Task draft must not be null");
        draft.validate();

        if (existing.getType() != draft.type()) {
            throw new IllegalArgumentException("Task type cannot be changed from "
                    + existing.getType() + " to " + draft.type());
        }

        existing.setTitle(draft.title());
        existing.setDescription(draft.description());
        existing.setPriority(draft.priority());
        existing.setCategoryId(draft.categoryId());

        if (existing instanceof DeadlineTask deadlineTask) {
            deadlineTask.setDueDate(draft.dueDate());
        } else if (existing instanceof ChecklistTask checklistTask) {
            checklistTask.syncItems(draft.checklistItems());
        }
    }
}
