package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Category;
import com.todoapp.service.CategoryService;
import com.todoapp.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Sidebar pane displaying available task categories and live task counts.
 * Allows filtering tasks by selecting a category.
 */
public class CategoryPane extends VBox {

    /**
     * Presentation item representing either "All Tasks" or a specific category.
     *
     * @param id       category UUID, or null for "All Tasks"
     * @param name     display name
     * @param count    count of matching tasks
     * @param colorHex hex color representation
     */
    public record CategoryEntry(UUID id, String name, int count, String colorHex) {
        /**
         * Checks whether this entry represents "All Tasks".
         *
         * @return true if all tasks entry
         */
        public boolean isAll() {
            return id == null;
        }
    }

    private final MainController controller;
    private final ObservableList<CategoryEntry> categoryEntries = FXCollections.observableArrayList();
    private final ListView<CategoryEntry> listView = new ListView<>(categoryEntries);
    private boolean updatingSelection;

    /**
     * Constructs a CategoryPane wired to the main controller.
     *
     * @param controller non-null main presentation controller
     */
    public CategoryPane(MainController controller) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");

        getStyleClass().add("category-pane");
        setSpacing(Theme.SPACING_SMALL);

        Label titleLabel = new Label("CATEGORIES");
        titleLabel.getStyleClass().add("category-title");

        listView.getStyleClass().add("category-list-view");
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CategoryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(Theme.SPACING_SMALL);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Region colorDot = new Region();
                    colorDot.setMinSize(8, 8);
                    colorDot.setMaxSize(8, 8);
                    colorDot.setStyle("-fx-background-color: " + (item.colorHex() != null ? item.colorHex() : "#757575")
                            + "; -fx-background-radius: 4px;");

                    Label nameLabel = new Label(item.name());
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, FxPriority.ALWAYS);

                    Label countLabel = new Label(String.valueOf(item.count()));
                    countLabel.getStyleClass().add("task-summary");

                    box.getChildren().addAll(colorDot, nameLabel, spacer, countLabel);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingSelection && newVal != null) {
                controller.onSelectCategory(newVal.id());
            }
        });

        getChildren().addAll(titleLabel, listView);
    }

    /**
     * Refreshes the category list entries and task counts according to current repository state.
     *
     * @param categories          current list of categories
     * @param taskService         task service for task count calculations
     * @param currentCategoryId   currently selected category ID, or null if all
     */
    public void updateCategories(List<Category> categories, TaskService taskService, UUID currentCategoryId) {
        updatingSelection = true;
        try {
            categoryEntries.clear();

            int allCount = taskService.getAll().size();
            CategoryEntry allEntry = new CategoryEntry(null, "All Tasks", allCount, "#1976D2");
            categoryEntries.add(allEntry);

            CategoryEntry entryToSelect = allEntry;

            for (Category category : categories) {
                int count = (int) taskService.getAll().stream()
                        .filter(t -> category.getId().equals(t.getCategoryId()))
                        .count();
                CategoryEntry entry = new CategoryEntry(category.getId(), category.getName(), count, category.getColorHex());
                categoryEntries.add(entry);

                if (category.getId().equals(currentCategoryId)) {
                    entryToSelect = entry;
                }
            }

            listView.getSelectionModel().select(entryToSelect);
        } finally {
            updatingSelection = false;
        }
    }
}
