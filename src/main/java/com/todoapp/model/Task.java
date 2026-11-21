package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract base class representing a task in the TODO system.
 * Encapsulates common attributes, lifecycle timestamps, and defines the
 * polymorphic contract implemented by concrete task subtypes.
 */
public abstract class Task {

    /**
     * Maximum character length permitted for task titles.
     */
    public static final int MAX_TITLE_LENGTH = 100;

    /**
     * Maximum character length permitted for task descriptions.
     */
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final UUID id;
    private String title;
    private String description;
    private Priority priority;
    private UUID categoryId;
    private boolean completed;
    private boolean pinned;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Full constructor initializing all task attributes.
     *
     * @param id          unique immutable task identifier
     * @param title       task title (1–100 characters)
     * @param description optional task description (≤2000 characters; null becomes "")
     * @param priority    priority level (defaults to MEDIUM if null)
     * @param categoryId  category reference ID (defaults to DEFAULT_ID if null)
     * @param completed   completion flag
     * @param pinned      pinned flag
     * @param createdAt   creation timestamp
     * @param updatedAt   last modified timestamp
     * @throws IllegalArgumentException if title or description constraints are violated
     */
    protected Task(UUID id,
                   String title,
                   String description,
                   Priority priority,
                   UUID categoryId,
                   boolean completed,
                   boolean pinned,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Task ID must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt timestamp must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt timestamp must not be null");
        setTitleInternal(title);
        setDescriptionInternal(description);
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.categoryId = categoryId != null ? categoryId : Category.DEFAULT_ID;
        this.completed = completed;
        this.pinned = pinned;
    }

    /**
     * Convenience constructor creating a new task with generated ID and timestamps.
     *
     * @param title       task title
     * @param description task description
     * @param priority    task priority
     * @param categoryId  category ID
     */
    protected Task(String title, String description, Priority priority, UUID categoryId) {
        this(UUID.randomUUID(),
                title,
                description,
                priority,
                categoryId,
                false,
                false,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    /**
     * Returns the unique immutable task identifier.
     *
     * @return task UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the task title.
     *
     * @return title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the task title and refreshes the {@code updatedAt} timestamp.
     *
     * @param title new title (1–100 characters)
     * @throws IllegalArgumentException if title is empty or exceeds limit
     */
    public void setTitle(String title) {
        setTitleInternal(title);
        touch();
    }

    private void setTitleInternal(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title must not be empty");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Task title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
        this.title = trimmed;
    }

    /**
     * Returns the task description. Never returns null.
     *
     * @return description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the task description and refreshes the {@code updatedAt} timestamp.
     *
     * @param description new description (≤2000 characters; null becomes "")
     * @throws IllegalArgumentException if description exceeds limit
     */
    public void setDescription(String description) {
        setDescriptionInternal(description);
        touch();
    }

    private void setDescriptionInternal(String description) {
        if (description == null) {
            this.description = "";
            return;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Task description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        this.description = trimmed;
    }

    /**
     * Returns the priority level.
     *
     * @return priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Updates the priority and touches the modification timestamp.
     *
     * @param priority non-null priority
     * @throws IllegalArgumentException if priority is null
     */
    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority must not be null");
        }
        this.priority = priority;
        touch();
    }

    /**
     * Returns the category ID reference.
     *
     * @return category UUID
     */
    public UUID getCategoryId() {
        return categoryId;
    }

    /**
     * Updates the category ID reference and touches the modification timestamp.
     *
     * @param categoryId non-null category UUID
     * @throws IllegalArgumentException if categoryId is null
     */
    public void setCategoryId(UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID must not be null");
        }
        this.categoryId = categoryId;
        touch();
    }

    /**
     * Indicates whether the task is completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the completion status and touches the modification timestamp.
     *
     * @param completed true if completed
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
        touch();
    }

    /**
     * Indicates whether the task is pinned.
     *
     * @return true if pinned, false otherwise
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * Sets the pinned flag and touches the modification timestamp.
     *
     * @param pinned true if pinned
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        touch();
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation LocalDateTime
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last modification timestamp.
     *
     * @return modification LocalDateTime
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the {@code updatedAt} timestamp to current system time.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Manually updates the {@code updatedAt} timestamp.
     *
     * @param timestamp explicit timestamp
     */
    public void touch(LocalDateTime timestamp) {
        if (timestamp != null) {
            this.updatedAt = timestamp;
        }
    }

    // ==========================================
    // Polymorphic Contract Methods
    // ==========================================

    /**
     * Returns the polymorphic {@link TaskType} identifier.
     *
     * @return task type
     */
    public abstract TaskType getType();

    /**
     * Returns a polymorphic user-facing summary string suitable for cards and list views.
     *
     * @return summary text
     */
    public abstract String getSummary();

    /**
     * Returns the task completion progress ratio as a double between 0.0 and 1.0.
     *
     * @return completion ratio
     */
    public abstract double getProgress();

    /**
     * Returns the deadline date if supported by this task type.
     *
     * @return optional deadline date
     */
    public abstract Optional<LocalDate> getDueDate();

    /**
     * Determines whether this task is overdue relative to a given reference date.
     *
     * @param today current reference date
     * @return true if overdue and incomplete
     */
    public abstract boolean isOverdue(LocalDate today);

    /**
     * Creates an isolated deep copy of this task, preserving the identical UUID and timestamps.
     *
     * @return deep copy of this task
     */
    public abstract Task copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", categoryId=" + categoryId +
                ", completed=" + completed +
                ", pinned=" + pinned +
                '}';
    }
}
