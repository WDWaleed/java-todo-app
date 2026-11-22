package com.todoapp.ui;

import com.todoapp.model.Category;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Modal dialog for creating, renaming, and recoloring task categories.
 * Features a name text field and a preset color swatch palette of distinct colors.
 */
public class CategoryDialog extends Dialog<CategoryDialog.CategoryData> {

    /**
     * Immutable data holder representing category form values.
     *
     * @param name     category name
     * @param colorHex hex color representation (e.g. #1976D2)
     */
    public record CategoryData(String name, String colorHex) {
        public CategoryData {
            Objects.requireNonNull(name, "Name must not be null");
            Objects.requireNonNull(colorHex, "Color must not be null");
        }
    }

    /**
     * Curated palette of preset category colors.
     */
    public static final List<String> PRESET_COLORS = List.of(
            "#1976D2", // Blue
            "#388E3C", // Green
            "#D32F2F", // Red
            "#F57C00", // Orange
            "#7B1FA2", // Purple
            "#0097A7", // Teal
            "#C2185B", // Pink
            "#616161"  // Grey
    );

    private final String initialName;
    private String selectedColorHex;
    private final Function<CategoryData, String> externalValidator;

    private TextField nameField;
    private Label errorLabel;
    private final List<Region> swatchNodes = new ArrayList<>();

    /**
     * Constructs a CategoryDialog in CREATE mode with default initial values.
     */
    public CategoryDialog() {
        this(null, null);
    }

    /**
     * Constructs a CategoryDialog with existing category values for editing/recoloring.
     *
     * @param existingCategory category to edit, or null for creation
     */
    public CategoryDialog(Category existingCategory) {
        this(existingCategory, null);
    }

    /**
     * Constructs a CategoryDialog with optional existing category and custom validator.
     *
     * @param existingCategory  category to edit, or null for creation
     * @param externalValidator optional external validation function returning error message or null
     */
    public CategoryDialog(Category existingCategory, Function<CategoryData, String> externalValidator) {
        this.initialName = (existingCategory != null) ? existingCategory.getName() : "";
        this.selectedColorHex = (existingCategory != null && existingCategory.getColorHex() != null)
                ? existingCategory.getColorHex().toUpperCase()
                : PRESET_COLORS.get(0);
        this.externalValidator = externalValidator;

        setTitle(existingCategory == null ? "New Category" : "Edit Category");
        setHeaderText(existingCategory == null ? "Create a new task category" : "Update category details");

        buildUi();
        applyStyles();
        configureValidationFilter();

        Platform.runLater(() -> nameField.requestFocus());
    }

    private void buildUi() {
        ButtonType okButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox content = new VBox(Theme.SPACING_MEDIUM);
        content.setPadding(new Insets(16));
        content.setPrefWidth(340);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        GridPane grid = new GridPane();
        grid.setHgap(Theme.SPACING_SMALL);
        grid.setVgap(Theme.SPACING_MEDIUM);

        // Name Field
        Label namePrompt = new Label("Name:");
        namePrompt.setStyle("-fx-font-weight: bold;");
        nameField = new TextField(initialName);
        nameField.setPromptText("Category name (1–30 characters)");
        grid.add(namePrompt, 0, 0);
        grid.add(nameField, 1, 0);

        // Color Swatches
        Label colorPrompt = new Label("Color:");
        colorPrompt.setStyle("-fx-font-weight: bold;");

        HBox swatchesBox = new HBox(6);
        swatchesBox.setAlignment(Pos.CENTER_LEFT);

        for (String hex : PRESET_COLORS) {
            Region swatch = createSwatchNode(hex);
            swatchNodes.add(swatch);
            swatchesBox.getChildren().add(swatch);
        }
        grid.add(colorPrompt, 0, 1);
        grid.add(swatchesBox, 1, 1);

        content.getChildren().addAll(errorLabel, grid);
        getDialogPane().setContent(content);

        updateSwatchSelections();

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new CategoryData(nameField.getText().trim(), selectedColorHex);
            }
            return null;
        });
    }

    private Region createSwatchNode(String hex) {
        Region swatch = new Region();
        swatch.setMinSize(24, 24);
        swatch.setPrefSize(24, 24);
        swatch.setMaxSize(24, 24);
        swatch.setCursor(javafx.scene.Cursor.HAND);
        swatch.setOnMouseClicked(e -> {
            selectedColorHex = hex;
            updateSwatchSelections();
        });
        return swatch;
    }

    private void updateSwatchSelections() {
        for (int i = 0; i < PRESET_COLORS.size(); i++) {
            String hex = PRESET_COLORS.get(i);
            Region swatch = swatchNodes.get(i);
            boolean isSelected = hex.equalsIgnoreCase(selectedColorHex);
            String borderStyle = isSelected
                    ? "-fx-border-color: #212121; -fx-border-width: 2.5px; -fx-border-radius: 6px;"
                    : "-fx-border-color: transparent; -fx-border-width: 2.5px; -fx-border-radius: 6px;";
            swatch.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 4px; " + borderStyle);
        }
    }

    private void configureValidationFilter() {
        Button okButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().stream()
                        .filter(b -> b.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                        .findFirst()
                        .orElse(ButtonType.OK)
        );

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText() != null ? nameField.getText().trim() : "";
            if (name.isEmpty()) {
                showError("Category name cannot be empty.");
                event.consume();
                return;
            }
            if (name.length() > 30) {
                showError("Category name must not exceed 30 characters.");
                event.consume();
                return;
            }

            if (externalValidator != null) {
                String validationError = externalValidator.apply(new CategoryData(name, selectedColorHex));
                if (validationError != null && !validationError.isBlank()) {
                    showError(validationError);
                    event.consume();
                    return;
                }
            }

            clearError();
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText(null);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void applyStyles() {
        URL css = getClass().getResource("styles.css");
        if (css != null) {
            getDialogPane().getStylesheets().add(css.toExternalForm());
        }
    }
}
