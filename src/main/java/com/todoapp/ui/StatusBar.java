package com.todoapp.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Bottom status bar presenting active and completed task counters,
 * along with transient system info or error messages.
 */
public class StatusBar extends HBox {

    private final Label countsLabel;
    private final Label messageLabel;

    /**
     * Constructs the StatusBar with default counters and empty messages.
     */
    public StatusBar() {
        getStyleClass().add("status-bar-pane");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(Theme.SPACING_LARGE);

        countsLabel = new Label("Active: 0 | Completed: 0");
        countsLabel.getStyleClass().add("status-counts");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        messageLabel = new Label("");

        getChildren().addAll(countsLabel, spacer, messageLabel);
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
