package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modal dialog for creating and editing tasks.
 * Supports switching between Simple and Deadline sections,
 * automatically closes upon successful task creation or update,
 * and maintains dialog visibility upon validation failure.
 */
public class TaskEditorDialog extends Dialog<TaskDraft> {

    private final MainController controller;
    private final boolean editMode;
    private final UUID existingTaskId;

    private ComboBox<TaskType> typeComboBox;
    private TextField titleField;
    private ComboBox<Priority> priorityComboBox;
    private ComboBox<Category> categoryComboBox;

    // Type-specific controls
    private StackPane typeSpecificStack;
    private VBox simplePane;
    private VBox deadlinePane;
    private DatePicker dueDatePicker;

    private Label errorLabel;

    /**
     * Constructs a TaskEditorDialog in CREATE mode.
     *
     * @param controller non-null main presentation controller
     */
    public TaskEditorDialog(MainController controller) {
        this(controller, null);
    }

    /**
     * Constructs a TaskEditorDialog in CREATE or EDIT mode.
     *
     * @param controller   non-null main presentation controller
     * @param existingTask task entity to edit, or null for creation
     */
    public TaskEditorDialog(MainController controller, Task existingTask) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.editMode = (existingTask != null);
        this.existingTaskId = editMode ? existingTask.getId() : null;

        setTitle(editMode ? "Edit Task" : "Create New Task");
        setHeaderText(editMode ? "Update task properties" : "Define task details and scheduling");

        buildUi(existingTask);
        applyStyles();
        configureValidationFilter();

        // Focus title field initially
        Platform.runLater(() -> titleField.requestFocus());
    }

    private void buildUi(Task existingTask) {
        GridPane grid = new GridPane();
        grid.setHgap(Theme.SPACING_MEDIUM);
        grid.setVgap(Theme.SPACING_SMALL);
        grid.setPadding(new Insets(Theme.PADDING_STANDARD));

        int row = 0;

        // Error message label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        grid.add(errorLabel, 0, row++, 2, 1);

        // Task Type Selector
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(TaskType.values());
        typeComboBox.setValue(existingTask != null ? existingTask.getType() : TaskType.SIMPLE);
        if (editMode) {
            typeComboBox.setDisable(true); // Type mutation prohibited in v1.0
        }
        grid.add(new Label("Task Type:"), 0, row);
        grid.add(typeComboBox, 1, row++);

        // Title Field
        titleField = new TextField();
        titleField.setPromptText("Enter title (1–100 characters)...");
        titleField.setPrefWidth(350);
        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
        }
        grid.add(new Label("Title: *"), 0, row);
        grid.add(titleField, 1, row++);

        // Priority Selector
        priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(Priority.values());
        priorityComboBox.setValue(existingTask != null ? existingTask.getPriority() : Priority.MEDIUM);
        grid.add(new Label("Priority:"), 0, row);
        grid.add(priorityComboBox, 1, row++);

        // Category Selector
        categoryComboBox = new ComboBox<>();
        List<Category> allCategories = controller.getCategoryService().getAll();
        categoryComboBox.getItems().addAll(allCategories);

        // Custom renderer showing color badge + name
        categoryComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Region dot = new Region();
                    dot.setMinSize(8, 8);
                    dot.setMaxSize(8, 8);
                    dot.setStyle("-fx-background-color: " + item.getColorHex() + "; -fx-background-radius: 4px;");
                    Label label = new Label(item.getName());
                    box.getChildren().addAll(dot, label);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
        categoryComboBox.setButtonCell(categoryComboBox.getCellFactory().call(null));

        // Default selection
        if (existingTask != null) {
            allCategories.stream()
                    .filter(c -> c.getId().equals(existingTask.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryComboBox::setValue);
        } else {
            allCategories.stream()
                    .filter(c -> Category.DEFAULT_ID.equals(c.getId()))
                    .findFirst()
                    .ifPresent(categoryComboBox::setValue);
        }
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryComboBox, 1, row++);

        // Type-Specific Section StackPane
        buildTypeSpecificSections(existingTask);
        grid.add(new Label("Details:"), 0, row);
        grid.add(typeSpecificStack, 1, row++);

        getDialogPane().setContent(grid);

        ButtonType confirmButtonType = new ButtonType(editMode ? "Save Changes" : "Create Task",
                ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> switchTypeSection(newVal));
        switchTypeSection(typeComboBox.getValue());

        setResultConverter(dialogButton -> {
            if (dialogButton != null && dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return buildDraft();
            }
            return null;
        });
    }

    private TaskDraft buildDraft() {
        String title = titleField.getText();
        Priority priority = priorityComboBox.getValue();
        Category category = categoryComboBox.getValue();
        UUID categoryId = category != null ? category.getId() : Category.DEFAULT_ID;
        TaskType type = typeComboBox.getValue();
        LocalDate dueDate = (type == TaskType.DEADLINE) ? dueDatePicker.getValue() : null;

        return new TaskDraft(type, title, priority, categoryId, dueDate);
    }

    private void buildTypeSpecificSections(Task existingTask) {
        typeSpecificStack = new StackPane();

        // 1. Simple task pane (no extra fields)
        simplePane = new VBox();
        Label simpleLabel = new Label("Standard task without calendar deadline.");
        simpleLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
        simplePane.getChildren().add(simpleLabel);

        // 2. Deadline task pane
        deadlinePane = new VBox(Theme.SPACING_SMALL);
        dueDatePicker = new DatePicker(LocalDate.now());
        dueDatePicker.setPromptText("Select due date");
        deadlinePane.getChildren().addAll(new Label("Target Due Date:"), dueDatePicker);

        // Populate existing type-specific values if in edit mode
        if (existingTask != null) {
            existingTask.getDueDate().ifPresent(dueDatePicker::setValue);
        }

        typeSpecificStack.getChildren().addAll(simplePane, deadlinePane);
    }

    private void switchTypeSection(TaskType type) {
        simplePane.setVisible(type == TaskType.SIMPLE);
        simplePane.setManaged(type == TaskType.SIMPLE);

        deadlinePane.setVisible(type == TaskType.DEADLINE);
        deadlinePane.setManaged(type == TaskType.DEADLINE);
    }

    private void configureValidationFilter() {
        Button okButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().stream()
                        .filter(b -> b.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                        .findFirst()
                        .orElse(ButtonType.OK)
        );

        // Allow Enter in the title field to trigger creation and close
        titleField.setOnAction(e -> okButton.fire());

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            TaskDraft draft = buildDraft();
            try {
                draft.validate();
                clearError();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
                event.consume();
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message != null ? message : "Validation failed. Please correct inputs.");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText(null);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void applyStyles() {
        URL cssResource = getClass().getResource("/com/todoapp/ui/styles.css");
        if (cssResource != null) {
            getDialogPane().getStylesheets().add(cssResource.toExternalForm());
        }
    }

    /**
     * Returns the title text field.
     *
     * @return TextField instance
     */
    public TextField getTitleField() {
        return titleField;
    }

    /**
     * Returns the error display label.
     *
     * @return Label instance
     */
    public Label getErrorLabel() {
        return errorLabel;
    }
}
