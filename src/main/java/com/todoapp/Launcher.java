package com.todoapp;

import javafx.application.Application;

/**
 * Standard Java entry point for launching the TODO List application.
 * <p>
 * This class acts as a launcher without extending {@link Application},
 * preventing JavaFX runtime component errors when packaged into a fat/shaded JAR.
 * </p>
 */
public final class Launcher {

    private Launcher() {
        // Prevent instantiation
    }

    /**
     * Launches the JavaFX application.
     * If graphical environment is unavailable, catches the failure and exits gracefully.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        try {
            Application.launch(App.class, args);
        } catch (UnsupportedOperationException ex) {
            System.err.println("Error: Failed to start JavaFX application. A graphical display environment is required.");
            System.exit(1);
        } catch (Throwable ex) {
            System.err.println("Error: Failed to start JavaFX application: " + ex.getMessage());
            System.exit(1);
        }
    }
}
