package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Category;
import com.todoapp.model.Task;
import com.todoapp.model.TaskType;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Custom {@link ListCell} rendering a task as a modern visual card.
 * Interacts with task entities strictly via polymorphic interface methods
 * (without {@code instanceof} pattern matching).
 */
public class TaskCell extends ListCell<Task> {

    private final MainController controller;
    private final Function<UUID, Category> categoryLookup;

    private final VBox card;
    private final Region priorityStripe;
    private final CheckBox completedCheckBox;
    private final Label titleLabel;
    private final Label pinLabel;
    private final Label categoryChip;
    private final Label summaryLabel;
    private final Label overdueLabel;
    private final ProgressBar progressBar;

    /**
     * Constructs a TaskCell with the given controller and default category lookup.
     *
     * @param controller non-null main presentation controller
     */
    public TaskCell(MainController controller) {
        this(controller, id -> {
            try {
                return controller.getCategoryService().get(id);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Constructs a TaskCell with custom category lookup function.
     *
     * @param controller     non-null main presentation controller
     * @param categoryLookup function resolving category entities from UUIDs
     */
    public TaskCell(MainController controller, Function<UUID, Category> categoryLookup) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.categoryLookup = categoryLookup != null ? categoryLookup : id -> null;

        card = new VBox(Theme.SPACING_XS);
        card.getStyleClass().add("task-card");

        // Top Header row: priority stripe, checkbox, title, pin, spacer, category chip
        HBox header = new HBox(Theme.SPACING_SMALL);
        header.setAlignment(Pos.CENTER_LEFT);

        priorityStripe = new Region();
        priorityStripe.getStyleClass().add("task-priority-stripe");

        completedCheckBox = new CheckBox();

        titleLabel = new Label();
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        pinLabel = new Label("📌");
        pinLabel.getStyleClass().add("pinned-indicator");
        pinLabel.setVisible(false);
        pinLabel.setManaged(false);

        categoryChip = new Label();
        categoryChip.getStyleClass().add("category-chip");
        categoryChip.setVisible(false);
        categoryChip.setManaged(false);

        header.getChildren().addAll(
                priorityStripe,
                completedCheckBox,
                titleLabel,
                pinLabel,
                categoryChip
        );

        // Body row: summary snippet, overdue badge, progress bar
        HBox detailsRow = new HBox(Theme.SPACING_SMALL);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("task-summary");
        HBox.setHgrow(summaryLabel, Priority.ALWAYS);

        overdueLabel = new Label("⚠️ Overdue");
        overdueLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 11px;");
        overdueLabel.setVisible(false);
        overdueLabel.setManaged(false);

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(120);
        progressBar.setMaxHeight(10);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        detailsRow.getChildren().addAll(summaryLabel, overdueLabel, progressBar);

        card.getChildren().addAll(header, detailsRow);
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);

        if (empty || task == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        // 1. Priority stripe color
        priorityStripe.setStyle("-fx-background-color: " + Theme.getPriorityColor(task.getPriority()) + ";");

        // 2. Checkbox state
        completedCheckBox.setSelected(task.isCompleted());

        // 3. Title and completion styling
        titleLabel.setText(task.getTitle());
        titleLabel.getStyleClass().removeAll("task-title", "task-title-completed");
        if (task.isCompleted()) {
            titleLabel.getStyleClass().add("task-title-completed");
        } else {
            titleLabel.getStyleClass().add("task-title");
        }

        // 4. Pin indicator
        boolean isPinned = task.isPinned();
        pinLabel.setVisible(isPinned);
        pinLabel.setManaged(isPinned);
        card.getStyleClass().remove("task-card-pinned");
        if (isPinned) {
            card.getStyleClass().add("task-card-pinned");
        }

        // 5. Category chip
        Category category = categoryLookup.apply(task.getCategoryId());
        if (category != null) {
            categoryChip.setText(category.getName());
            String color = category.getColorHex() != null ? category.getColorHex() : "#757575";
            categoryChip.setStyle("-fx-background-color: " + color + ";");
            categoryChip.setVisible(true);
            categoryChip.setManaged(true);
        } else {
            categoryChip.setVisible(false);
            categoryChip.setManaged(false);
        }

        // 6. Polymorphic summary snippet
        String summary = task.getSummary();
        if (summary != null && !summary.isBlank()) {
            summaryLabel.setText(summary);
            summaryLabel.setVisible(true);
            summaryLabel.setManaged(true);
        } else {
            summaryLabel.setVisible(false);
            summaryLabel.setManaged(false);
        }

        // 7. Polymorphic overdue status
        boolean overdue = task.isOverdue(LocalDate.now());
        overdueLabel.setVisible(overdue);
        overdueLabel.setManaged(overdue);

        // 8. Polymorphic progress bar (displayed for checklist tasks)
        if (task.getType() == TaskType.CHECKLIST) {
            progressBar.setProgress(task.getProgress());
            progressBar.setVisible(true);
            progressBar.setManaged(true);
        } else {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }

        setGraphic(card);
        setText(null);
    }

    /**
     * Returns the completion checkbox node.
     *
     * @return CheckBox instance
     */
    public CheckBox getCompletedCheckBox() {
        return completedCheckBox;
    }

    /**
     * Returns the card root layout container.
     *
     * @return VBox card container
     */
    public VBox getCard() {
        return card;
    }
}
