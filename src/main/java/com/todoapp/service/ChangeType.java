package com.todoapp.service;

/**
 * Enumeration of change event types dispatched to {@link TaskChangeListener} observers.
 */
public enum ChangeType {

    /**
     * A new task was created and saved.
     */
    CREATED,

    /**
     * An existing task was updated.
     */
    UPDATED,

    /**
     * A task was removed and moved to the undo buffer.
     */
    DELETED,

    /**
     * A previously deleted task was restored from the undo buffer.
     */
    RESTORED,

    /**
     * A category was created, modified, or deleted (resulting in task reassignment).
     */
    CATEGORY_CHANGED
}
