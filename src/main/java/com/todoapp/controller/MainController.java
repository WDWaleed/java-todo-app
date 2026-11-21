package com.todoapp.controller;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.service.CategoryService;
import com.todoapp.service.ChangeType;
import com.todoapp.service.SortOption;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Presentation controller coordinating UI events and domain service invocations.
 * Manages active filtering, searching, sorting, and notifies registered {@link ViewListener}s.
 * Free of any JavaFX dependencies to allow comprehensive headless unit testing.
 */
public class MainController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final List<ViewListener> viewListeners = new ArrayList<>();
    private ViewState currentState;

    /**
     * Constructs a MainController backed by the specified task and category services.
     * Registers itself as a change listener to automatically refresh views upon data mutations.
     *
     * @param taskService     non-null task service
     * @param categoryService non-null category service
     */
    public MainController(TaskService taskService, CategoryService categoryService) {
        this.taskService = Objects.requireNonNull(taskService, "TaskService must not be null");
        this.categoryService = Objects.requireNonNull(categoryService, "CategoryService must not be null");
        this.currentState = ViewState.initial();

        // Listen for domain changes from services to automatically trigger refresh
        this.taskService.addListener(this::onDomainChanged);
        this.categoryService.addListener(this::onDomainChanged);
    }

    private void onDomainChanged(ChangeType type, UUID id) {
        refresh();
    }

    /**
     * Registers a view listener to receive view change events.
     *
     * @param listener non-null view listener
     */
    public void addListener(ViewListener listener) {
        Objects.requireNonNull(listener, "ViewListener must not be null");
        viewListeners.add(listener);
    }

    /**
     * Unregisters a view listener.
     *
     * @param listener view listener to remove
     */
    public void removeListener(ViewListener listener) {
        viewListeners.remove(listener);
    }

    /**
     * Returns the current view presentation state.
     *
     * @return current ViewState
     */
    public ViewState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the underlying task service.
     *
     * @return task service
     */
    public TaskService getTaskService() {
        return taskService;
    }

    /**
     * Returns the underlying category service.
     *
     * @return category service
     */
    public CategoryService getCategoryService() {
        return categoryService;
    }

    /**
     * Queries the task and category services with the current view state and publishes
     * an update to all registered view listeners.
     */
    public void refresh() {
        List<Task> tasks = taskService.query(currentState.query());
        List<Category> categories = categoryService.getAll();
        for (ViewListener listener : viewListeners) {
            listener.onViewChanged(tasks, categories, currentState);
        }
    }

    /**
     * Handles category selection change.
     * Passing {@code null} selects "All Categories".
     *
     * @param categoryId category UUID, or null for all
     */
    public void onSelectCategory(UUID categoryId) {
        this.currentState = currentState.withQuery(currentState.query().withCategory(categoryId));
        refresh();
    }

    /**
     * Handles search keyword change.
     *
     * @param searchText keyword query (trimmed in query predicate)
     */
    public void onSearch(String searchText) {
        this.currentState = currentState.withQuery(currentState.query().withSearch(searchText));
        refresh();
    }

    /**
     * Handles sort strategy and direction change.
     *
     * @param sort      sort criteria
     * @param ascending true for ascending, false for descending
     */
    public void onSort(SortOption sort, boolean ascending) {
        this.currentState = currentState.withQuery(currentState.query().withSort(sort, ascending));
        refresh();
    }

    /**
     * Handles task completion status filter change.
     *
     * @param status status filter (ALL, ACTIVE, COMPLETED)
     */
    public void onStatusFilter(StatusFilter status) {
        this.currentState = currentState.withQuery(currentState.query().withStatus(status));
        refresh();
    }

    /**
     * Handles priority filter change.
     * Passing {@code null} clears the priority filter (matches any priority).
     *
     * @param priority priority level, or null for any
     */
    public void onPriorityFilter(Priority priority) {
        this.currentState = currentState.withQuery(currentState.query().withPriority(priority));
        refresh();
    }

    /**
     * Updates the currently selected task ID in view state and publishes a refresh.
     *
     * @param taskId task UUID, or null if selection is cleared
     */
    public void onSelectTask(UUID taskId) {
        this.currentState = currentState.withSelectedTaskId(taskId);
        refresh();
    }

    /**
     * Sets an informational status message and triggers a view refresh.
     *
     * @param message informational text
     */
    public void setInfoMessage(String message) {
        this.currentState = currentState.withInfoMessage(message);
        refresh();
    }

    /**
     * Sets an error message and triggers a view refresh.
     *
     * @param errorMessage error text
     */
    public void setErrorMessage(String errorMessage) {
        this.currentState = currentState.withErrorMessage(errorMessage);
        refresh();
    }

    /**
     * Clears all status and error messages and triggers a view refresh.
     */
    public void clearMessages() {
        this.currentState = currentState.clearMessages();
        refresh();
    }
}
