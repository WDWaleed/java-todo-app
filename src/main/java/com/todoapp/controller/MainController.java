package com.todoapp.controller;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.service.CategoryService;
import com.todoapp.service.ChangeType;
import com.todoapp.service.NotFoundException;
import com.todoapp.service.SortOption;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskService;
import com.todoapp.service.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    /**
     * Creates a new task from the submitted draft.
     * Catches validation exceptions, updates view state error message, and triggers refresh.
     *
     * @param draft non-null task draft
     * @return Optional containing created task, or empty if validation failed
     */
    public Optional<Task> onCreateTask(TaskDraft draft) {
        try {
            Task created = taskService.create(draft);
            this.currentState = currentState.withSelectedTaskId(created.getId())
                    .withInfoMessage("Task created: " + created.getTitle());
            refresh();
            return Optional.of(created);
        } catch (ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Updates an existing task with new draft content.
     * Catches validation or not-found exceptions, updates view state error message, and triggers refresh.
     *
     * @param id    target task UUID
     * @param draft non-null updated task draft
     * @return Optional containing updated task, or empty if validation/lookup failed
     */
    public Optional<Task> onUpdateTask(UUID id, TaskDraft draft) {
        try {
            Task updated = taskService.update(id, draft);
            this.currentState = currentState.withSelectedTaskId(updated.getId())
                    .withInfoMessage("Task updated: " + updated.getTitle());
            refresh();
            return Optional.of(updated);
        } catch (NotFoundException | ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Deletes a task by ID, buffering it for undo.
     *
     * @param id target task UUID
     * @return true if deleted, false if not found
     */
    public boolean onDeleteTask(UUID id) {
        try {
            Task deleted = taskService.delete(id);
            this.currentState = currentState.withSelectedTaskId(null)
                    .withInfoMessage("Task deleted: " + deleted.getTitle() + " — Undo");
            refresh();
            return true;
        } catch (NotFoundException | ValidationException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return false;
        }
    }

    /**
     * Restores the most recently deleted task from the undo buffer.
     *
     * @return Optional containing restored task, or empty if buffer was empty
     */
    public Optional<Task> onUndoDelete() {
        try {
            Optional<Task> restored = taskService.undoDelete();
            if (restored.isPresent()) {
                this.currentState = currentState.withSelectedTaskId(restored.get().getId())
                        .withInfoMessage("Task restored: " + restored.get().getTitle());
            } else {
                this.currentState = currentState.withInfoMessage("Nothing to undo");
            }
            refresh();
            return restored;
        } catch (Exception e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Toggles the completion status of a task.
     *
     * @param id target task UUID
     */
    public void onToggleComplete(UUID id) {
        try {
            taskService.toggleComplete(id);
            this.currentState = currentState.clearMessages();
            refresh();
        } catch (NotFoundException | ValidationException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
        }
    }

    /**
     * Updates the priority level of a task.
     *
     * @param id       target task UUID
     * @param priority new priority level
     */
    public void onSetPriority(UUID id, Priority priority) {
        try {
            taskService.setPriority(id, priority);
            this.currentState = currentState.clearMessages();
            refresh();
        } catch (NotFoundException | ValidationException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
        }
    }

    /**
     * Toggles the pinned status of a task.
     *
     * @param id target task UUID
     */
    public void onTogglePinned(UUID id) {
        try {
            taskService.togglePinned(id);
            this.currentState = currentState.clearMessages();
            refresh();
        } catch (NotFoundException | ValidationException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
        }
    }

    /**
     * Creates a new category.
     *
     * @param name     category name
     * @param colorHex hex color code (#RRGGBB)
     * @return Optional containing created category, or empty if validation failed
     */
    public Optional<Category> onCreateCategory(String name, String colorHex) {
        try {
            Category created = categoryService.create(name, colorHex);
            this.currentState = currentState.withInfoMessage("Category created: " + created.getName());
            refresh();
            return Optional.of(created);
        } catch (ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Renames an existing category.
     *
     * @param id      category UUID
     * @param newName new name
     * @return Optional containing updated category, or empty if validation/lookup failed
     */
    public Optional<Category> onRenameCategory(UUID id, String newName) {
        try {
            Category updated = categoryService.rename(id, newName);
            this.currentState = currentState.withInfoMessage("Category renamed: " + updated.getName());
            refresh();
            return Optional.of(updated);
        } catch (NotFoundException | ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Updates the color code of an existing category.
     *
     * @param id          category UUID
     * @param newColorHex new hex color string
     * @return Optional containing updated category, or empty if validation/lookup failed
     */
    public Optional<Category> onRecolorCategory(UUID id, String newColorHex) {
        try {
            Category updated = categoryService.recolor(id, newColorHex);
            this.currentState = currentState.withInfoMessage("Category recolored: " + updated.getName());
            refresh();
            return Optional.of(updated);
        } catch (NotFoundException | ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return Optional.empty();
        }
    }

    /**
     * Deletes a category and reassigns its tasks to General.
     * If the deleted category was currently selected, selection falls back to All.
     *
     * @param id category UUID to delete
     * @return true if deleted, false if validation/lookup failed
     */
    public boolean onDeleteCategory(UUID id) {
        try {
            Category deleted = categoryService.delete(id);
            if (currentState.query().categoryId() != null && currentState.query().categoryId().equals(id)) {
                this.currentState = currentState.withQuery(currentState.query().withCategory(null));
            }
            this.currentState = currentState.withInfoMessage("Category deleted: " + deleted.getName());
            refresh();
            return true;
        } catch (NotFoundException | ValidationException | IllegalArgumentException e) {
            this.currentState = currentState.withErrorMessage(e.getMessage());
            refresh();
            return false;
        }
    }
}
