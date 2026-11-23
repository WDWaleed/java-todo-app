package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Priority;
import com.todoapp.service.SortOption;
import com.todoapp.service.StatusFilter;
import com.todoapp.service.TaskQuery;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Top toolbar containing quick action buttons, search text field with live debounced filtering,
 * sorting controls, status/priority filters, and a clear filters action.
 */
public class ToolbarPane extends HBox {

    private final MainController controller;
    private final Button newTaskButton;
    private final TextField searchField;
    private final Button clearSearchButton;
    private final ComboBox<SortOption> sortComboBox;
    private final ToggleButton sortDirectionButton;
    private final ComboBox<StatusFilter> statusFilterComboBox;
    private final ComboBox<String> priorityFilterComboBox;
    private final Button clearFiltersButton;

    private boolean updatingUi;

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
        newTaskButton.setTooltip(new Tooltip("Create a new task (Ctrl+N)"));
        newTaskButton.setOnAction(e -> {
            TaskEditorDialog dialog = new TaskEditorDialog(controller);
            dialog.showAndWait().ifPresent(controller::onCreateTask);
        });

        // Search Field with debounce ~200 ms and Clear (✕) button
        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                controller.onSearch("");
                e.consume();
            }
        });

        clearSearchButton = new Button("✕");
        clearSearchButton.getStyleClass().add("clear-search-button");
        clearSearchButton.setTooltip(new Tooltip("Clear search"));
        clearSearchButton.setVisible(false);
        clearSearchButton.setManaged(false);
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            controller.onSearch("");
        });

        PauseTransition searchDebounce = new PauseTransition(Duration.millis(200));
        searchDebounce.setOnFinished(e -> {
            if (!updatingUi) {
                controller.onSearch(searchField.getText());
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasText = newVal != null && !newVal.isEmpty();
            clearSearchButton.setVisible(hasText);
            clearSearchButton.setManaged(hasText);
            if (!updatingUi) {
                searchDebounce.playFromStart();
            }
        });

        HBox searchContainer = new HBox(2);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getChildren().addAll(searchField, clearSearchButton);

        // Spacer between primary actions and filter/sort controls
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Sort ComboBox with clean display labels
        sortComboBox = new ComboBox<>();
        sortComboBox.setTooltip(new Tooltip("Sort tasks by"));
        sortComboBox.getItems().addAll(SortOption.values());
        sortComboBox.setValue(SortOption.CREATED);
        sortComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SortOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });
        sortComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SortOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        // Sort Direction Toggle Button
        sortDirectionButton = new ToggleButton("↓ Desc");
        sortDirectionButton.setTooltip(new Tooltip("Toggle sort direction (Ascending / Descending)"));
        sortDirectionButton.setSelected(false);
        sortDirectionButton.setOnAction(e -> {
            if (!updatingUi) {
                boolean asc = sortDirectionButton.isSelected();
                sortDirectionButton.setText(asc ? "↑ Asc" : "↓ Desc");
                controller.onSort(sortComboBox.getValue(), asc);
            }
        });

        sortComboBox.setOnAction(e -> {
            if (!updatingUi) {
                controller.onSort(sortComboBox.getValue(), sortDirectionButton.isSelected());
            }
        });

        // Status Filter ComboBox with clean display labels
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.setTooltip(new Tooltip("Filter by completion status"));
        statusFilterComboBox.getItems().addAll(StatusFilter.values());
        statusFilterComboBox.setValue(StatusFilter.ALL);
        statusFilterComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StatusFilter item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });
        statusFilterComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StatusFilter item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });
        statusFilterComboBox.setOnAction(e -> {
            if (!updatingUi) {
                controller.onStatusFilter(statusFilterComboBox.getValue());
            }
        });

        // Priority Filter ComboBox
        priorityFilterComboBox = new ComboBox<>();
        priorityFilterComboBox.setTooltip(new Tooltip("Filter by priority level"));
        priorityFilterComboBox.getItems().addAll("All Priorities", "High", "Medium", "Low");
        priorityFilterComboBox.setValue("All Priorities");
        priorityFilterComboBox.setOnAction(e -> {
            if (!updatingUi) {
                String selected = priorityFilterComboBox.getValue();
                Priority p = "All Priorities".equals(selected) ? null : Priority.fromLabel(selected);
                controller.onPriorityFilter(p);
            }
        });

        // Clear Filters Button
        clearFiltersButton = new Button("Clear filters");
        clearFiltersButton.getStyleClass().add("clear-filters-button");
        clearFiltersButton.setTooltip(new Tooltip("Reset all filters, sorting and search"));
        clearFiltersButton.setOnAction(e -> controller.onClearFilters());

        getChildren().addAll(
                newTaskButton,
                searchContainer,
                spacer,
                sortComboBox,
                sortDirectionButton,
                statusFilterComboBox,
                priorityFilterComboBox,
                clearFiltersButton
        );
    }

    /**
     * Synchronizes toolbar control values with the active query in view state.
     *
     * @param query current active task query
     */
    public void updateFromQuery(TaskQuery query) {
        updatingUi = true;
        try {
            String querySearch = query.searchText() != null ? query.searchText() : "";
            if (!searchField.getText().equals(querySearch)) {
                searchField.setText(querySearch);
            }
            clearSearchButton.setVisible(!querySearch.isEmpty());
            clearSearchButton.setManaged(!querySearch.isEmpty());

            sortComboBox.setValue(query.sort());
            sortDirectionButton.setSelected(query.ascending());
            sortDirectionButton.setText(query.ascending() ? "↑ Asc" : "↓ Desc");

            statusFilterComboBox.setValue(query.status());

            if (query.priority() != null) {
                priorityFilterComboBox.setValue(query.priority().getLabel());
            } else {
                priorityFilterComboBox.setValue("All Priorities");
            }
        } finally {
            updatingUi = false;
        }
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

    /**
     * Returns the clear filters button.
     *
     * @return clear filters button
     */
    public Button getClearFiltersButton() {
        return clearFiltersButton;
    }
}
