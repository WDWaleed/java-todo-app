package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete implementation of a standard, simple task containing a title.
 */
public class SimpleTask extends Task {

    /**
     * Constructs a SimpleTask with complete parameters.
     *
     * @param id         task UUID
     * @param title      task title
     * @param priority   task priority
     * @param categoryId category UUID
     * @param completed  completion flag
     * @param pinned     pinned flag
     * @param createdAt  creation timestamp
     * @param updatedAt  modification timestamp
     */
    public SimpleTask(UUID id,
                      String title,
                      Priority priority,
                      UUID categoryId,
                      boolean completed,
                      boolean pinned,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        super(id, title, priority, categoryId, completed, pinned, createdAt, updatedAt);
    }

    /**
     * Convenience constructor generating a random UUID and current timestamps.
     *
     * @param title      task title
     * @param priority   task priority
     * @param categoryId category UUID
     */
    public SimpleTask(String title, Priority priority, UUID categoryId) {
        super(title, priority, categoryId);
    }

    @Override
    public TaskType getType() {
        return TaskType.SIMPLE;
    }

    @Override
    public String getSummary() {
        return "";
    }

    @Override
    public double getProgress() {
        return isCompleted() ? 1.0 : 0.0;
    }

    @Override
    public Optional<LocalDate> getDueDate() {
        return Optional.empty();
    }

    @Override
    public boolean isOverdue(LocalDate today) {
        return false;
    }

    @Override
    public SimpleTask copy() {
        return new SimpleTask(
                getId(),
                getTitle(),
                getPriority(),
                getCategoryId(),
                isCompleted(),
                isPinned(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
