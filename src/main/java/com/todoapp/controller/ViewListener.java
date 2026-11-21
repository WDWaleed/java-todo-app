package com.todoapp.controller;

import com.todoapp.model.Category;
import com.todoapp.model.Task;

import java.util.List;

/**
 * Observer interface implemented by presentation views to be notified
 * when tasks, categories, or presentation state change.
 */
@FunctionalInterface
public interface ViewListener {

    /**
     * Invoked when the view must be refreshed with updated tasks, categories, and state.
     *
     * @param tasks      current ordered list of tasks matching active query
     * @param categories current list of available categories
     * @param state      current view presentation state
     */
    void onViewChanged(List<Task> tasks, List<Category> categories, ViewState state);
}
