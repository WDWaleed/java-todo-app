package com.todoapp.service;

import java.util.UUID;

/**
 * Observer interface notified when domain entities (tasks or categories) are modified.
 */
@FunctionalInterface
public interface TaskChangeListener {

    /**
     * Invoked when a task or category mutation occurs.
     *
     * @param type   the kind of change event
     * @param taskId the affected task ID or category ID (may be null for bulk changes)
     */
    void onTasksChanged(ChangeType type, UUID taskId);
}
