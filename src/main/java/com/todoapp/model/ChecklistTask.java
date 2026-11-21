package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Task subtype containing an ordered list of checkable sub-items.
 * Automatically synchronizes completion status between sub-items and parent task.
 */
public class ChecklistTask extends Task {

    private final List<ChecklistItem> items = new ArrayList<>();

    /**
     * Constructs a ChecklistTask with all attributes and an initial set of items.
     *
     * @param id          task UUID
     * @param title       task title
     * @param description task description
     * @param priority    task priority
     * @param categoryId  category UUID
     * @param completed   completion flag
     * @param pinned      pinned flag
     * @param createdAt   creation timestamp
     * @param updatedAt   modification timestamp
     * @param items       initial sub-items (may be null or empty)
     */
    public ChecklistTask(UUID id,
                         String title,
                         String description,
                         Priority priority,
                         UUID categoryId,
                         boolean completed,
                         boolean pinned,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt,
                         List<ChecklistItem> items) {
        super(id, title, description, priority, categoryId, completed, pinned, createdAt, updatedAt);
        if (items != null) {
            for (ChecklistItem item : items) {
                this.items.add(item.copy());
            }
        }
    }

    /**
     * Convenience constructor creating a new checklist task.
     *
     * @param title       task title
     * @param description task description
     * @param priority    task priority
     * @param categoryId  category UUID
     * @param items       initial sub-items
     */
    public ChecklistTask(String title,
                         String description,
                         Priority priority,
                         UUID categoryId,
                         List<ChecklistItem> items) {
        super(title, description, priority, categoryId);
        if (items != null) {
            for (ChecklistItem item : items) {
                this.items.add(item.copy());
            }
        }
    }

    /**
     * Returns an unmodifiable view of the checklist items.
     *
     * @return unmodifiable list of items
     */
    public List<ChecklistItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds a new sub-item to the checklist.
     * If the task was previously completed, adding an undone item sets completed to false.
     *
     * @param text item description (1–100 chars)
     * @return the created ChecklistItem
     */
    public ChecklistItem addItem(String text) {
        ChecklistItem item = new ChecklistItem(text);
        items.add(item);
        if (isCompleted() && !item.isDone()) {
            super.setCompleted(false);
        }
        touch();
        return item;
    }

    /**
     * Removes a sub-item by its unique identifier.
     *
     * @param itemId UUID of the item to remove
     * @return true if item was removed, false otherwise
     */
    public boolean removeItem(UUID itemId) {
        if (itemId == null) return false;
        boolean removed = items.removeIf(item -> item.getId().equals(itemId));
        if (removed) {
            checkAndUpdateAutoCompletion();
            touch();
        }
        return removed;
    }

    /**
     * Toggles the completion state of a sub-item.
     * Automatically adjusts the parent task completion status:
     * - If all items become done, the parent task is marked completed.
     * - If any item becomes undone while parent is completed, parent becomes incomplete.
     *
     * @param itemId UUID of item to toggle
     * @return true if toggled successfully, false if item not found
     */
    public boolean toggleItem(UUID itemId) {
        if (itemId == null) return false;
        for (ChecklistItem item : items) {
            if (item.getId().equals(itemId)) {
                item.setDone(!item.isDone());
                checkAndUpdateAutoCompletion();
                touch();
                return true;
            }
        }
        return false;
    }

    private void checkAndUpdateAutoCompletion() {
        if (items.isEmpty()) {
            return;
        }
        boolean allDone = true;
        for (ChecklistItem item : items) {
            if (!item.isDone()) {
                allDone = false;
                break;
            }
        }
        if (allDone && !isCompleted()) {
            super.setCompleted(true);
        } else if (!allDone && isCompleted()) {
            super.setCompleted(false);
        }
    }

    @Override
    public void setCompleted(boolean completed) {
        super.setCompleted(completed);
        for (ChecklistItem item : items) {
            item.setDone(completed);
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.CHECKLIST;
    }

    @Override
    public String getSummary() {
        if (items.isEmpty()) {
            return "0 items";
        }
        long doneCount = items.stream().filter(ChecklistItem::isDone).count();
        return doneCount + "/" + items.size() + " done";
    }

    @Override
    public double getProgress() {
        if (items.isEmpty()) {
            return isCompleted() ? 1.0 : 0.0;
        }
        long doneCount = items.stream().filter(ChecklistItem::isDone).count();
        return (double) doneCount / items.size();
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
    public ChecklistTask copy() {
        List<ChecklistItem> itemCopies = new ArrayList<>();
        for (ChecklistItem item : items) {
            itemCopies.add(item.copy());
        }
        return new ChecklistTask(
                getId(),
                getTitle(),
                getDescription(),
                getPriority(),
                getCategoryId(),
                isCompleted(),
                isPinned(),
                getCreatedAt(),
                getUpdatedAt(),
                itemCopies
        );
    }
}
