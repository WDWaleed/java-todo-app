package com.todoapp.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single checkable item within a {@link ChecklistTask}.
 */
public class ChecklistItem {

    /**
     * Maximum character length allowed for item text.
     */
    public static final int MAX_TEXT_LENGTH = 100;

    private final UUID id;
    private String text;
    private boolean done;

    /**
     * Constructs a ChecklistItem with a specified ID, text, and completion status.
     *
     * @param id   the immutable unique identifier
     * @param text description of the sub-item (1–100 chars)
     * @param done completion flag
     * @throws IllegalArgumentException if id is null or text violates constraints
     */
    public ChecklistItem(UUID id, String text, boolean done) {
        this.id = Objects.requireNonNull(id, "Item ID must not be null");
        setText(text);
        this.done = done;
    }

    /**
     * Convenience constructor generating a random UUID and initial undone status.
     *
     * @param text description of the sub-item (1–100 chars)
     */
    public ChecklistItem(String text) {
        this(UUID.randomUUID(), text, false);
    }

    /**
     * Returns the immutable item identifier.
     *
     * @return item UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the sub-item text.
     *
     * @return text content
     */
    public String getText() {
        return text;
    }

    /**
     * Updates the sub-item text after trimming and validation.
     *
     * @param text new text (1–100 characters)
     * @throws IllegalArgumentException if text is blank or exceeds 100 characters
     */
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Checklist item text must not be empty");
        }
        String trimmed = text.trim();
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Checklist item text must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
        this.text = trimmed;
    }

    /**
     * Indicates whether this sub-item is completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Sets the completion status of this sub-item.
     *
     * @param done new completion state
     */
    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Creates a deep copy of this checklist item.
     *
     * @return duplicate item with identical ID, text, and completion state
     */
    public ChecklistItem copy() {
        return new ChecklistItem(this.id, this.text, this.done);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChecklistItem that = (ChecklistItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChecklistItem{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", done=" + done +
                '}';
    }
}
