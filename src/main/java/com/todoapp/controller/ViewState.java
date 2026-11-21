package com.todoapp.controller;

import com.todoapp.service.TaskQuery;

import java.util.UUID;

/**
 * Immutable record capturing the current presentation state of the application view.
 *
 * @param query          active task query defining filtering, searching, and sorting
 * @param selectedTaskId unique ID of the currently selected task, or null if none
 * @param infoMessage    transient informational status message, or null
 * @param errorMessage   transient error message, or null
 */
public record ViewState(
        TaskQuery query,
        UUID selectedTaskId,
        String infoMessage,
        String errorMessage
) {

    /**
     * Compact constructor normalizing null query to the default query.
     */
    public ViewState {
        query = query != null ? query : TaskQuery.defaultQuery();
    }

    /**
     * Returns an initial view state with default query and no active selection or messages.
     *
     * @return initial ViewState
     */
    public static ViewState initial() {
        return new ViewState(TaskQuery.defaultQuery(), null, null, null);
    }

    /**
     * Creates a copy of this state with an updated TaskQuery.
     *
     * @param newQuery updated query
     * @return new ViewState instance
     */
    public ViewState withQuery(TaskQuery newQuery) {
        return new ViewState(newQuery, this.selectedTaskId, this.infoMessage, this.errorMessage);
    }

    /**
     * Creates a copy of this state with an updated selected task ID.
     *
     * @param newSelectedTaskId task UUID, or null
     * @return new ViewState instance
     */
    public ViewState withSelectedTaskId(UUID newSelectedTaskId) {
        return new ViewState(this.query, newSelectedTaskId, this.infoMessage, this.errorMessage);
    }

    /**
     * Creates a copy of this state with an updated informational message.
     *
     * @param message informational text, or null
     * @return new ViewState instance
     */
    public ViewState withInfoMessage(String message) {
        return new ViewState(this.query, this.selectedTaskId, message, null);
    }

    /**
     * Creates a copy of this state with an updated error message.
     *
     * @param error error text, or null
     * @return new ViewState instance
     */
    public ViewState withErrorMessage(String error) {
        return new ViewState(this.query, this.selectedTaskId, null, error);
    }

    /**
     * Creates a copy of this state clearing all messages.
     *
     * @return new ViewState instance
     */
    public ViewState clearMessages() {
        return new ViewState(this.query, this.selectedTaskId, null, null);
    }
}
