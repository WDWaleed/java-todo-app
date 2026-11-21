package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.controller.ViewListener;
import com.todoapp.controller.ViewState;
import com.todoapp.model.Category;
import com.todoapp.model.Task;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.Objects;

/**
 * Main application window content root implementing {@link ViewListener}.
 * Coordinates layout structure:
 * <ul>
 *     <li>Top: {@link ToolbarPane}</li>
 *     <li>Left: {@link CategoryPane}</li>
 *     <li>Center: {@link TaskListPane}</li>
 *     <li>Bottom: {@link StatusBar}</li>
 * </ul>
 */
public class MainView extends BorderPane implements ViewListener {

    private final MainController controller;
    private final ToolbarPane toolbar;
    private final CategoryPane categoryPane;
    private final TaskListPane taskListPane;
    private final StatusBar statusBar;

    /**
     * Constructs the MainView, configures subcomponents, registers with controller,
     * and triggers initial presentation refresh.
     *
     * @param controller non-null main presentation controller
     */
    public MainView(MainController controller) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");

        this.toolbar = new ToolbarPane(controller);
        this.categoryPane = new CategoryPane(controller);
        this.taskListPane = new TaskListPane(controller);
        this.statusBar = new StatusBar();

        setTop(toolbar);
        setLeft(categoryPane);
        setCenter(taskListPane);
        setBottom(statusBar);

        // Register with controller to receive view change events
        this.controller.addListener(this);

        // Trigger initial data load into views
        this.controller.refresh();
    }

    @Override
    public void onViewChanged(List<Task> tasks, List<Category> categories, ViewState state) {
        Runnable updateAction = () -> {
            taskListPane.setTasks(tasks);
            categoryPane.updateCategories(categories, controller.getTaskService(), state.query().categoryId());

            int activeCount = controller.getTaskService().countActive();
            int completedCount = controller.getTaskService().countCompleted();
            statusBar.update(activeCount, completedCount, state.infoMessage(), state.errorMessage());
        };

        if (Platform.isFxApplicationThread()) {
            updateAction.run();
        } else {
            Platform.runLater(updateAction);
        }
    }

    /**
     * Returns the toolbar pane component.
     *
     * @return ToolbarPane instance
     */
    public ToolbarPane getToolbar() {
        return toolbar;
    }

    /**
     * Returns the category sidebar component.
     *
     * @return CategoryPane instance
     */
    public CategoryPane getCategoryPane() {
        return categoryPane;
    }

    /**
     * Returns the center task list pane component.
     *
     * @return TaskListPane instance
     */
    public TaskListPane getTaskListPane() {
        return taskListPane;
    }

    /**
     * Returns the bottom status bar component.
     *
     * @return StatusBar instance
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }
}
