package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Task;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskQuery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Center pane hosting the task list view, empty-state placeholder,
 * and temporary card cell rendering.
 */
public class TaskListPane extends VBox {

    private final MainController controller;
    private final ObservableList<Task> tasksList = FXCollections.observableArrayList();
    private final ListView<Task> listView = new ListView<>(tasksList);
    private final Label placeholderLabel = new Label("No tasks yet — click New Task");

    /**
     * Constructs a TaskListPane wired to the main controller.
     *
     * @param controller non-null main presentation controller
     */
    public TaskListPane(MainController controller) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");

        getStyleClass().add("task-list-pane");
        VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS);

        listView.getStyleClass().add("task-list-view");
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);

        placeholderLabel.getStyleClass().add("placeholder-label");
        listView.setPlaceholder(placeholderLabel);

        // Use custom polymorphic card cell factory
        listView.setCellFactory(lv -> new TaskCell(controller));

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                controller.onSelectTask(newVal.getId());
            } else {
                controller.onSelectTask(null);
            }
        });

        // Delete key shortcut per M09.4
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                Task selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    TaskCell.confirmAndDelete(controller, selected);
                }
            }
        });

        getChildren().add(listView);
    }

    /**
     * Replaces the currently displayed tasks list with the given collection,
     * preserving selection where possible.
     *
     * @param tasks non-null list of tasks
     */
    public void setTasks(List<Task> tasks) {
        setTasks(tasks, controller.getCurrentState().query(), !controller.getTaskService().getAll().isEmpty());
    }

    /**
     * Replaces the currently displayed tasks list and updates placeholder text
     * depending on whether filters are active.
     *
     * @param tasks                 non-null list of matching tasks
     * @param query                 active task query
     * @param hasAnyTasksInStorage  true if any tasks exist in storage
     */
    public void setTasks(List<Task> tasks, TaskQuery query, boolean hasAnyTasksInStorage) {
        UUID selectedId = controller.getCurrentState().selectedTaskId();
        tasksList.setAll(tasks);
        if (selectedId != null) {
            tasksList.stream()
                    .filter(t -> selectedId.equals(t.getId()))
                    .findFirst()
                    .ifPresent(matching -> listView.getSelectionModel().select(matching));
        }

        boolean hasFilters = (query != null) && (
                query.categoryId() != null
                || (query.searchText() != null && !query.searchText().isBlank())
                || query.status() != StatusFilter.ALL
                || query.priority() != null
        );

        if (hasFilters || hasAnyTasksInStorage) {
            placeholderLabel.setText("No tasks match your filters");
        } else {
            placeholderLabel.setText("No tasks yet — click New Task");
        }
    }

    /**
     * Returns the underlying task ListView.
     *
     * @return ListView instance
     */
    public ListView<Task> getListView() {
        return listView;
    }
}
