package com.todoapp.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Bottom status bar presenting active and completed task counters,
 * along with transient system info or error messages, and an undo action button.
 */
public class StatusBar extends HBox {

    private final Label countsLabel;
    private final Label messageLabel;
    private final Button undoButton;
    private final PauseTransition undoTimer;
    private Runnable currentUndoAction;

    /**
     * Constructs the StatusBar with default counters and empty messages.
     */
    public StatusBar() {
        getStyleClass().add("status-bar-pane");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(Theme.SPACING_MEDIUM);

        countsLabel = new Label("Active: 0 | Completed: 0");
        countsLabel.getStyleClass().add("status-counts");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        messageLabel = new Label("");

        undoButton = new Button("Undo");
        undoButton.getStyleClass().add("status-undo-button");
        undoButton.setVisible(false);
        undoButton.setManaged(false);

        undoTimer = new PauseTransition(Duration.seconds(8));
        undoTimer.setOnFinished(e -> hideUndo());

        undoButton.setOnAction(e -> {
            Runnable action = currentUndoAction;
            hideUndo();
            if (action != null) {
                action.run();
            }
        });

        getChildren().addAll(countsLabel, spacer, messageLabel, undoButton);
    }

    /**
     * Displays the Undo action button for approximately 8 seconds.
     *
     * @param onUndo callback to execute when Undo is clicked
     */
    public void showUndo(Runnable onUndo) {
        this.currentUndoAction = onUndo;
        undoButton.setVisible(true);
        undoButton.setManaged(true);
        undoTimer.playFromStart();
    }

    /**
     * Hides and cancels the Undo action button.
     */
    public void hideUndo() {
        undoTimer.stop();
        undoButton.setVisible(false);
        undoButton.setManaged(false);
        this.currentUndoAction = null;
    }

    /**
     * Returns the Undo button node.
     *
     * @return Button instance
     */
    public Button getUndoButton() {
        return undoButton;
    }

    /**
     * Updates the status bar counters and status message.
     *
     * @param activeCount    number of uncompleted active tasks
     * @param completedCount number of completed tasks
     * @param infoMessage    informational message to display, or null
     * @param errorMessage   error message to display, or null
     */
    public void update(int activeCount, int completedCount, String infoMessage, String errorMessage) {
        countsLabel.setText("Active: " + activeCount + " | Completed: " + completedCount);

        messageLabel.getStyleClass().removeAll("status-info-message", "status-error-message");
        if (errorMessage != null && !errorMessage.isBlank()) {
            messageLabel.setText(errorMessage);
            messageLabel.getStyleClass().add("status-error-message");
        } else if (infoMessage != null && !infoMessage.isBlank()) {
            messageLabel.setText(infoMessage);
            messageLabel.getStyleClass().add("status-info-message");
        } else {
            messageLabel.setText("");
        }
    }
}
