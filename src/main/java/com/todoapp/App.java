package com.todoapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main JavaFX Application class for the TODO List Desktop Application.
 * Configures the primary window stage, scene, and default styles.
 */
public class App extends Application {

    /**
     * The application display name.
     */
    public static final String APP_NAME = "TODO App";

    /**
     * Default window width in pixels.
     */
    public static final double DEFAULT_WIDTH = 800;

    /**
     * Default window height in pixels.
     */
    public static final double DEFAULT_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        URL cssResource = getClass().getResource("/com/todoapp/ui/styles.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
