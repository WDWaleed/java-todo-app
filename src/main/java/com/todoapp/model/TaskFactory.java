package com.todoapp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     *
     * @param draft validated task draft
     * @return concrete Task instance (SimpleTask, ChecklistTask, or DeadlineTask)
     * @throws IllegalArgumentException if draft is null or fails validation
     */
    public static Task create(TaskDraft draft) {
        Objects.requireNonNull(draft, "Task draft must not be null");
        draft.validate();

        return switch (draft.type()) {
            case SIMPLE -> new SimpleTask(
                    draft.title(),
                    draft.description(),
                    draft.priority(),
                    draft.categoryId()
            );
            case CHECKLIST -> {
                List<ChecklistItem> items = new ArrayList<>();
                for (String text : draft.checklistItems()) {
                    items.add(new ChecklistItem(text));
                }
                yield new ChecklistTask(
                        draft.title(),
                        draft.description(),
                        draft.priority(),
                        draft.categoryId(),
                        items
                );
            }
            case DEADLINE -> new DeadlineTask(
                    draft.title(),
                    draft.description(),
                    draft.priority(),
                    draft.categoryId(),
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
