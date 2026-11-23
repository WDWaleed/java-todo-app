package com.todoapp.ui;

import com.todoapp.App;
import com.todoapp.controller.MainController;
import com.todoapp.controller.ViewState;
import com.todoapp.model.Task;
import com.todoapp.service.StatusFilter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Top-level application menu bar providing File, Edit, View, and Help menus.
 */
public class AppMenuBar extends MenuBar {

    private final MainController controller;
    private final Supplier<Task> selectedTaskSupplier;
    private final MenuItem undoMenuItem;
    private final CheckMenuItem showCompletedItem;

    /**
     * Constructs the AppMenuBar wired to the controller and selected task supplier.
     *
     * @param controller           non-null main presentation controller
     * @param selectedTaskSupplier supplier returning currently selected task
     */
    public AppMenuBar(MainController controller, Supplier<Task> selectedTaskSupplier) {
        this.controller = Objects.requireNonNull(controller, "Controller must not be null");
        this.selectedTaskSupplier = selectedTaskSupplier != null ? selectedTaskSupplier : () -> null;

        getStyleClass().add("app-menu-bar");

        // --- File Menu ---
        Menu fileMenu = new Menu("File");
        MenuItem newTaskItem = new MenuItem("New Task");
        newTaskItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        newTaskItem.setOnAction(e -> {
            TaskEditorDialog dialog = new TaskEditorDialog(controller);
            dialog.showAndWait().ifPresent(controller::onCreateTask);
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(newTaskItem, new SeparatorMenuItem(), exitItem);

        // --- Edit Menu ---
        Menu editMenu = new Menu("Edit");
        MenuItem editTaskItem = new MenuItem("Edit Task");
        editTaskItem.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        editTaskItem.setOnAction(e -> {
            Task selected = this.selectedTaskSupplier.get();
            if (selected != null) {
                TaskCell.openEditDialog(controller, selected);
            } else {
                controller.setInfoMessage("Please select a task to edit.");
            }
        });

        MenuItem deleteTaskItem = new MenuItem("Delete Task");
        deleteTaskItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        deleteTaskItem.setOnAction(e -> {
            Task selected = this.selectedTaskSupplier.get();
            if (selected != null) {
                TaskCell.confirmAndDelete(controller, selected);
            } else {
                controller.setInfoMessage("Please select a task to delete.");
            }
        });

        undoMenuItem = new MenuItem("Undo Delete");
        undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        undoMenuItem.setDisable(true);
        undoMenuItem.setOnAction(e -> controller.onUndoDelete());

        editMenu.getItems().addAll(editTaskItem, deleteTaskItem, new SeparatorMenuItem(), undoMenuItem);

        // --- View Menu ---
        Menu viewMenu = new Menu("View");
        showCompletedItem = new CheckMenuItem("Show Completed Tasks");
        showCompletedItem.setSelected(true);
        showCompletedItem.setOnAction(e -> {
            controller.onStatusFilter(showCompletedItem.isSelected() ? StatusFilter.ALL : StatusFilter.ACTIVE);
        });

        MenuItem clearFiltersItem = new MenuItem("Clear Filters");
        clearFiltersItem.setOnAction(e -> controller.onClearFilters());

        viewMenu.getItems().addAll(showCompletedItem, clearFiltersItem);

        // --- Help Menu ---
        Menu helpMenu = new Menu("Help");
        MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts");
        shortcutsItem.setOnAction(e -> showShortcutsDialog());

        MenuItem aboutItem = new MenuItem("About " + App.APP_NAME);
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(shortcutsItem, new SeparatorMenuItem(), aboutItem);

        getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
    }

    /**
     * Updates dynamic menu item states according to the current view state.
     *
     * @param state   current view state
     * @param canUndo true if undo is available
     */
    public void updateState(ViewState state, boolean canUndo) {
        undoMenuItem.setDisable(!canUndo);
        if (state != null && state.query() != null) {
            showCompletedItem.setSelected(state.query().status() != StatusFilter.ACTIVE);
        }
    }

    /**
     * Displays the About dialog.
     */
    public static void showAboutDialog() {
        String version = App.class.getPackage().getImplementationVersion();
        if (version == null || version.isBlank()) {
            version = "1.0.0";
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About " + App.APP_NAME);
        alert.setHeaderText(App.APP_NAME + " - Version " + version);
        alert.setContentText(
                "A modern, offline-first TODO desktop application built with JavaFX.\n\n"
                        + "Author: Software Construction Lab (OEL)\n"
                        + "Course: Software Construction\n"
                        + "Toolkit: JavaFX 21 (OpenJFX)\n"
                        + "Persistence: JSON file storage with thread-safe debounced autosave\n\n"
                        + "Designed with Google Keep & Microsoft To-Do principles."
        );
        alert.showAndWait();
    }

    /**
     * Displays the Keyboard Shortcuts help dialog.
     */
    public static void showShortcutsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Available Keyboard Shortcuts");
        alert.setContentText(
                "• Ctrl + N : Create a new task\n"
                        + "• Ctrl + Z : Undo last deleted task\n"
                        + "• Ctrl + F : Focus search bar\n"
                        + "• Enter / F2 : Edit selected task\n"
                        + "• Delete : Delete selected task\n"
                        + "• Space : Toggle complete for selected task\n"
                        + "• Esc : Clear filters / dismiss dialogs"
        );
        alert.showAndWait();
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public CheckMenuItem getShowCompletedItem() {
        return showCompletedItem;
    }
}
