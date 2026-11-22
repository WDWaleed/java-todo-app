package com.todoapp;

import com.todoapp.controller.MainController;
import com.todoapp.repository.InMemoryCategoryRepository;
import com.todoapp.repository.InMemoryTaskRepository;
import com.todoapp.service.CategoryService;
import com.todoapp.service.TaskService;
import com.todoapp.storage.AppData;
import com.todoapp.storage.AutosaveListener;
import com.todoapp.storage.DataSync;
import com.todoapp.storage.JsonStorage;
import com.todoapp.storage.StorageException;
import com.todoapp.ui.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Clock;
import java.util.List;

/**
 * Main JavaFX Application class for the TODO List Desktop Application.
 * Configures application bootstrap, wires repository, service, storage, and controller layers,
 * and handles persistent autosave lifecycle management.
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

    /**
     * Minimum window width in pixels.
     */
    public static final double MIN_WIDTH = 800;

    /**
     * Minimum window height in pixels.
     */
    public static final double MIN_HEIGHT = 500;

    private JsonStorage storage;
    private AutosaveListener autosave;
    private MainController controller;

    /**
     * Default constructor for JavaFX Application lifecycle.
     */
    public App() {
        super();
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> rawArgs = getParameters() != null ? getParameters().getRaw() : List.of();
        AppConfig config = AppConfig.parse(rawArgs);

        this.storage = new JsonStorage(config.dataPath());

        // Load persisted snapshot from storage
        AppData appData;
        try {
            appData = storage.load();
        } catch (StorageException e) {
            appData = AppData.empty();
        }

        // Initialize in-memory repositories
        InMemoryTaskRepository taskRepo = new InMemoryTaskRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();

        // Populate repositories from snapshot
        DataSync.loadInto(taskRepo, categoryRepo, appData);

        // Optional development seed if requested and repository is empty
        if (config.seed() && taskRepo.count() == 0) {
            DevSeed.seedIfEmpty(taskRepo, categoryRepo);
        }

        // Initialize business domain services
        CategoryService categoryService = new CategoryService(categoryRepo, taskRepo);
        TaskService taskService = new TaskService(taskRepo, categoryRepo, Clock.systemDefaultZone());

        // Initialize presentation controller
        this.controller = new MainController(taskService, categoryService);

        // Configure debounced autosave listener with background error reporting
        this.autosave = new AutosaveListener(
                storage,
                taskRepo,
                categoryRepo,
                errorMsg -> Platform.runLater(() -> controller.setErrorMessage(errorMsg))
        );
        taskService.addListener(autosave);
        categoryService.addListener(autosave);

        // Build main JavaFX view
        MainView mainView = new MainView(controller);
        Scene scene = new Scene(mainView, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Global shortcuts via scene accelerators
        KeyCombination searchShortcut = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        scene.getAccelerators().put(searchShortcut, () -> {
            mainView.getToolbar().getSearchField().requestFocus();
            mainView.getToolbar().getSearchField().selectAll();
        });

        URL cssResource = getClass().getResource("/com/todoapp/ui/styles.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }

        primaryStage.setTitle(APP_NAME);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Show data load warning once if file was corrupted or had unrecognized records
        storage.getLastLoadWarning().ifPresent(warning -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Data Warning");
            alert.setHeaderText("Notice regarding data file");
            alert.setContentText(warning);
            alert.show();
        });
    }

    @Override
    public void stop() {
        if (autosave != null) {
            autosave.close();
        }
    }
}
