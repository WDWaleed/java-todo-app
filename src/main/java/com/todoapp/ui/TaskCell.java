package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskType;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
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
        completedCheckBox.setOnAction(e -> {
            if (getItem() != null) {
                controller.onToggleComplete(getItem().getId());
            }
        });

        titleLabel = new Label();
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        pinLabel = new Label("📌");
        pinLabel.getStyleClass().add("pinned-indicator");
        pinLabel.setCursor(javafx.scene.Cursor.HAND);
        pinLabel.setOnMouseClicked(event -> {
            if (getItem() != null) {
                controller.onTogglePinned(getItem().getId());
                event.consume();
            }
        });

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
        HBox.setHgrow(summaryLabel, javafx.scene.layout.Priority.ALWAYS);

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

        // Double-click to open edit dialog per M09.3
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && getItem() != null) {
                openEditDialog(controller, getItem());
            }
        });
    }

    /**
     * Opens the task editor dialog pre-populated with the specified task data.
     *
     * @param controller non-null main presentation controller
     * @param task       non-null task to edit
     */
    public static void openEditDialog(MainController controller, Task task) {
        if (task == null) return;
        Task current;
        try {
            current = controller.getTaskService().get(task.getId());
        } catch (Exception e) {
            controller.setInfoMessage("Task was removed or is no longer available.");
            controller.refresh();
            return;
        }
        TaskEditorDialog dialog = new TaskEditorDialog(controller, current);
        dialog.showAndWait();
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
        completedCheckBox.setTooltip(new Tooltip(task.isCompleted() ? "Mark incomplete (Space)" : "Mark complete (Space)"));

        // 3. Title and completion styling, plus card dimming
        titleLabel.setText(task.getTitle());
        titleLabel.getStyleClass().removeAll("task-title", "task-title-completed");
        card.getStyleClass().remove("task-card-completed");
        if (task.isCompleted()) {
            titleLabel.getStyleClass().add("task-title-completed");
            card.getStyleClass().add("task-card-completed");
        } else {
            titleLabel.getStyleClass().add("task-title");
        }

        // 4. Pin indicator with click toggle
        boolean isPinned = task.isPinned();
        pinLabel.setText(isPinned ? "📌" : "📍");
        pinLabel.setStyle(isPinned ? "-fx-opacity: 1.0;" : "-fx-opacity: 0.35;");
        pinLabel.setTooltip(new Tooltip(isPinned ? "Unpin task" : "Pin task to top"));
        pinLabel.setVisible(true);
        pinLabel.setManaged(true);
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

        // 7. Polymorphic overdue and due-soon status
        LocalDate today = LocalDate.now();
        boolean overdue = task.isOverdue(today);
        Optional<LocalDate> dueDate = task.getDueDate();
        boolean dueSoon = !task.isCompleted() && !overdue && dueDate.isPresent()
                && !dueDate.get().isBefore(today)
                && !dueDate.get().isAfter(today.plusDays(2));

        overdueLabel.getStyleClass().removeAll("task-badge-overdue", "task-badge-due-soon");
        if (overdue) {
            overdueLabel.setText("⚠️ Overdue");
            overdueLabel.getStyleClass().add("task-badge-overdue");
            overdueLabel.setVisible(true);
            overdueLabel.setManaged(true);
        } else if (dueSoon) {
            overdueLabel.setText("⏰ Due soon");
            overdueLabel.getStyleClass().add("task-badge-due-soon");
            overdueLabel.setVisible(true);
            overdueLabel.setManaged(true);
        } else {
            overdueLabel.setVisible(false);
            overdueLabel.setManaged(false);
        }

        // 8. Polymorphic progress bar (displayed for checklist tasks)
        if (task.getType() == TaskType.CHECKLIST) {
            progressBar.setProgress(task.getProgress());
            progressBar.setVisible(true);
            progressBar.setManaged(true);
        } else {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
        }

        // Attach dynamic context menu
        setContextMenu(createContextMenu(task));

        setGraphic(card);
        setText(null);
    }

    private ContextMenu createContextMenu(Task task) {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit Task");
        editItem.setOnAction(e -> openEditDialog(controller, task));

        MenuItem completeItem = new MenuItem(task.isCompleted() ? "Mark Incomplete" : "Mark Complete");
        completeItem.setOnAction(e -> controller.onToggleComplete(task.getId()));

        MenuItem pinItem = new MenuItem(task.isPinned() ? "Unpin Task" : "Pin Task");
        pinItem.setOnAction(e -> controller.onTogglePinned(task.getId()));

        Menu priorityMenu = new Menu("Priority");
        for (Priority p : Priority.values()) {
            MenuItem pItem = new MenuItem(p.getLabel());
            pItem.setOnAction(e -> controller.onSetPriority(task.getId(), p));
            priorityMenu.getItems().add(pItem);
        }

        MenuItem deleteItem = new MenuItem("Delete Task");
        deleteItem.setOnAction(e -> confirmAndDelete(controller, task));

        menu.getItems().addAll(
                editItem,
                completeItem,
                pinItem,
                priorityMenu,
                new SeparatorMenuItem(),
                deleteItem
        );
        return menu;
    }

    /**
     * Prompts the user with a confirmation alert and deletes the task upon approval.
     *
     * @param controller non-null main presentation controller
     * @param task       non-null task to delete
     * @return true if deleted, false if cancelled
     */
    public static boolean confirmAndDelete(MainController controller, Task task) {
        if (task == null) return false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete '" + task.getTitle() + "'?");
        alert.setContentText("This can be undone from Edit ▸ Undo.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return controller.onDeleteTask(task.getId());
        }
        return false;
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
