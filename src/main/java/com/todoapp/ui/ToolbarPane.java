package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Priority;
import com.todoapp.service.SortOption;
import com.todoapp.service.StatusFilter;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * Top toolbar containing quick action buttons, search text field, sorting controls,
 * and status/priority filters.
 */
public class ToolbarPane extends HBox {

    private final MainController controller;
    private final Button newTaskButton;
    private final TextField searchField;
    private final ComboBox<SortOption> sortComboBox;
    private final ToggleButton sortDirectionButton;
    private final ComboBox<StatusFilter> statusFilterComboBox;
    private final ComboBox<String> priorityFilterComboBox;

    /**
     * Constructs a ToolbarPane wired to the application controller.
     *
     * @param controller non-null main presentation controller
     */
    public ToolbarPane(MainController controller) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");

        getStyleClass().add("toolbar-pane");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(Theme.SPACING_MEDIUM);

        // New Task Button
        newTaskButton = new Button("＋ New Task");
        newTaskButton.getStyleClass().add("new-task-button");
        newTaskButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Task");
            alert.setHeaderText("Coming Soon");
            alert.setContentText("Task creation dialog will be enabled in milestone M09.");
            alert.showAndWait();
        });

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> controller.onSearch(newVal));

        // Spacer between primary actions and filter/sort controls
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Sort ComboBox
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(SortOption.values());
        sortComboBox.setValue(SortOption.CREATED);

        // Sort Direction Toggle Button
        sortDirectionButton = new ToggleButton("↓ Desc");
        sortDirectionButton.setSelected(false);
        sortDirectionButton.setOnAction(e -> {
            boolean asc = sortDirectionButton.isSelected();
            sortDirectionButton.setText(asc ? "↑ Asc" : "↓ Desc");
            controller.onSort(sortComboBox.getValue(), asc);
        });

        sortComboBox.setOnAction(e -> controller.onSort(sortComboBox.getValue(), sortDirectionButton.isSelected()));

        // Status Filter ComboBox
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.getItems().addAll(StatusFilter.values());
        statusFilterComboBox.setValue(StatusFilter.ALL);
        statusFilterComboBox.setOnAction(e -> controller.onStatusFilter(statusFilterComboBox.getValue()));

        // Priority Filter ComboBox
        priorityFilterComboBox = new ComboBox<>();
        priorityFilterComboBox.getItems().addAll("All Priorities", "High", "Medium", "Low");
        priorityFilterComboBox.setValue("All Priorities");
        priorityFilterComboBox.setOnAction(e -> {
            String selected = priorityFilterComboBox.getValue();
            Priority p = "All Priorities".equals(selected) ? null : Priority.fromLabel(selected);
            controller.onPriorityFilter(p);
        });

        getChildren().addAll(
                newTaskButton,
                searchField,
                spacer,
                sortComboBox,
                sortDirectionButton,
                statusFilterComboBox,
                priorityFilterComboBox
        );
    }

    /**
     * Returns the "New Task" action button.
     *
     * @return new task button
     */
    public Button getNewTaskButton() {
        return newTaskButton;
    }

    /**
     * Returns the search input field.
     *
     * @return search text field
     */
    public TextField getSearchField() {
        return searchField;
    }
}
