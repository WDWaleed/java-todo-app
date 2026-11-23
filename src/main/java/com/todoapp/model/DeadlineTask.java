package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Task subtype linked to a calendar deadline date.
 * Computes overdue status and provides deadline summaries.
 */
public class DeadlineTask extends Task {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM");

    private LocalDate dueDate;

    /**
     * Constructs a DeadlineTask with full attribute set.
     *
     * @param id         task UUID
     * @param title      task title
     * @param priority   task priority
     * @param categoryId category UUID
     * @param completed  completion flag
     * @param pinned     pinned flag
     * @param createdAt  creation timestamp
     * @param updatedAt  modification timestamp
     * @param dueDate    non-null deadline date
     * @throws IllegalArgumentException if dueDate is null
     */
    public DeadlineTask(UUID id,
                        String title,
                        Priority priority,
                        UUID categoryId,
                        boolean completed,
                        boolean pinned,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt,
                        LocalDate dueDate) {
        super(id, title, priority, categoryId, completed, pinned, createdAt, updatedAt);
        setDueDateInternal(dueDate);
    }

    /**
     * Convenience constructor creating a new deadline task.
     *
     * @param title      task title
     * @param priority   task priority
     * @param categoryId category UUID
     * @param dueDate    non-null deadline date
     */
    public DeadlineTask(String title,
                        Priority priority,
                        UUID categoryId,
                        LocalDate dueDate) {
        super(title, priority, categoryId);
        setDueDateInternal(dueDate);
    }

    /**
     * Returns the raw deadline date.
     *
     * @return due date
     */
    public LocalDate getDueDateValue() {
        return dueDate;
    }

    /**
     * Updates the deadline date and touches modification timestamp.
     *
     * @param dueDate non-null deadline date
     * @throws IllegalArgumentException if dueDate is null
     */
    public void setDueDate(LocalDate dueDate) {
        setDueDateInternal(dueDate);
        touch();
    }

    private void setDueDateInternal(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(dueDate, "Due date must not be null for deadline tasks");
    }

    @Override
    public TaskType getType() {
        return TaskType.DEADLINE;
    }

    @Override
    public String getSummary() {
        String formatted = "Due " + dueDate.format(DATE_FORMATTER);
        if (isOverdue(LocalDate.now())) {
            return formatted + " (overdue)";
        }
        return formatted;
    }

    @Override
    public double getProgress() {
        return isCompleted() ? 1.0 : 0.0;
    }

    @Override
    public Optional<LocalDate> getDueDate() {
        return Optional.of(dueDate);
    }

    @Override
    public boolean isOverdue(LocalDate today) {
        Objects.requireNonNull(today, "Reference date must not be null");
        return !isCompleted() && dueDate.isBefore(today);
    }

    @Override
    public DeadlineTask copy() {
        return new DeadlineTask(
                getId(),
                getTitle(),
                getPriority(),
                getCategoryId(),
                isCompleted(),
                isPinned(),
                getCreatedAt(),
                getUpdatedAt(),
                dueDate
        );
    }
}
