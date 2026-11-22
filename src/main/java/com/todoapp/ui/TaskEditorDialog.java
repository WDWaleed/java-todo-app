package com.todoapp.ui;

import com.todoapp.controller.MainController;
import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Modal dialog for creating and editing tasks.
 * Supports dynamic switching between Simple, Checklist, and Deadline sections,
 * and maintains dialog visibility upon validation failure.
 */
public class TaskEditorDialog extends Dialog<TaskDraft> {

    private final MainController controller;
    private final boolean editMode;
    private final UUID existingTaskId;

    private ComboBox<TaskType> typeComboBox;
    private TextField titleField;
    private TextArea descriptionArea;
    private ComboBox<Priority> priorityComboBox;
    private ComboBox<Category> categoryComboBox;

    // Type-specific controls
    private StackPane typeSpecificStack;
    private VBox simplePane;
    private VBox checklistPane;
    private VBox deadlinePane;

    private ObservableList<String> checklistItemsList;
    private ListView<String> checklistListView;
    private TextField newChecklistItemField;
    private DatePicker dueDatePicker;

    private Label errorLabel;
    private TaskDraft committedDraft;

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
        // Prevent Enter in title field from closing the dialog
        titleField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
            }
        });
        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
        }
        grid.add(new Label("Title: *"), 0, row);
        grid.add(titleField, 1, row++);

        // Description Area
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add description (optional)...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        if (existingTask != null) {
            descriptionArea.setText(existingTask.getDescription());
        }
        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

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

        setResultConverter(dialogButton -> dialogButton == confirmButtonType ? committedDraft : null);
    }

    private void buildTypeSpecificSections(Task existingTask) {
        typeSpecificStack = new StackPane();

        // 1. Simple task pane (no extra fields)
        simplePane = new VBox();
        Label simpleLabel = new Label("Standard text task without deadline or checklist.");
        simpleLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
        simplePane.getChildren().add(simpleLabel);

        // 2. Checklist task pane
        checklistPane = new VBox(Theme.SPACING_SMALL);
        checklistItemsList = FXCollections.observableArrayList();
        checklistListView = new ListView<>(checklistItemsList);
        checklistListView.setPrefHeight(120);

        HBox addBox = new HBox(Theme.SPACING_SMALL);
        newChecklistItemField = new TextField();
        newChecklistItemField.setPromptText("Add new checklist item...");
        HBox.setHgrow(newChecklistItemField, javafx.scene.layout.Priority.ALWAYS);

        Button addItemBtn = new Button("Add");
        addItemBtn.setOnAction(e -> addChecklistItem());
        newChecklistItemField.setOnAction(e -> addChecklistItem());

        Button removeItemBtn = new Button("Remove");
        removeItemBtn.setOnAction(e -> {
            String selected = checklistListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                checklistItemsList.remove(selected);
            }
        });

        addBox.getChildren().addAll(newChecklistItemField, addItemBtn, removeItemBtn);
        checklistPane.getChildren().addAll(checklistListView, addBox);

        // 3. Deadline task pane
        deadlinePane = new VBox(Theme.SPACING_SMALL);
        dueDatePicker = new DatePicker(LocalDate.now());
        dueDatePicker.setPromptText("Select due date");
        deadlinePane.getChildren().addAll(new Label("Target Due Date:"), dueDatePicker);

        // Populate existing type-specific values if in edit mode
        if (existingTask != null) {
            if (existingTask.getType() == TaskType.DEADLINE && existingTask.getDueDate().isPresent()) {
                dueDatePicker.setValue(existingTask.getDueDate().get());
            } else if (existingTask.getType() == TaskType.CHECKLIST) {
                // Checklist items will be populated in M09.3 edit mode
            }
        }

        typeSpecificStack.getChildren().addAll(simplePane, checklistPane, deadlinePane);
    }

    private void addChecklistItem() {
        String text = newChecklistItemField.getText();
        if (text != null && !text.trim().isEmpty()) {
            checklistItemsList.add(text.trim());
            newChecklistItemField.clear();
        }
    }

    private void switchTypeSection(TaskType type) {
        simplePane.setVisible(type == TaskType.SIMPLE);
        simplePane.setManaged(type == TaskType.SIMPLE);

        checklistPane.setVisible(type == TaskType.CHECKLIST);
        checklistPane.setManaged(type == TaskType.CHECKLIST);

        deadlinePane.setVisible(type == TaskType.DEADLINE);
        deadlinePane.setManaged(type == TaskType.DEADLINE);
    }

    private void configureValidationFilter() {
        Button okButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().get(0)
        );

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            Priority priority = priorityComboBox.getValue();
            Category category = categoryComboBox.getValue();
            UUID categoryId = category != null ? category.getId() : Category.DEFAULT_ID;
            TaskType type = typeComboBox.getValue();

            LocalDate dueDate = (type == TaskType.DEADLINE) ? dueDatePicker.getValue() : null;
            List<String> items = (type == TaskType.CHECKLIST) ? new ArrayList<>(checklistItemsList) : null;

            TaskDraft draft;
            try {
                draft = new TaskDraft(type, title, description, priority, categoryId, dueDate, items);
                draft.validate();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
                event.consume();
                return;
            }

            if (!editMode) {
                Optional<Task> created = controller.onCreateTask(draft);
                if (created.isEmpty()) {
                    showError(controller.getCurrentState().errorMessage());
                    event.consume();
                    return;
                }
            } else {
                Optional<Task> updated = controller.onUpdateTask(existingTaskId, draft);
                if (updated.isEmpty()) {
                    showError(controller.getCurrentState().errorMessage());
                    event.consume();
                    return;
                }
            }

            committedDraft = draft;
        });
    }

    private void showError(String message) {
        errorLabel.setText(message != null ? message : "Validation failed. Please correct inputs.");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
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
