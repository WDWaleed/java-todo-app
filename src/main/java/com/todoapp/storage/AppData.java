package com.todoapp.storage;

import com.todoapp.model.Category;
import com.todoapp.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable data snapshot representing the entire persistent state of the application.
 *
 * @param schemaVersion persistent schema version for backward and forward compatibility
 * @param categories    list of categories preserved in storage
 * @param tasks         list of polymorphic tasks preserved in storage
 */
public record AppData(int schemaVersion, List<Category> categories, List<Task> tasks) {

    /**
     * Current schema version supported by this application release.
     */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * Compact constructor guaranteeing defensive copying, non-nullity, and immutability.
     */
    public AppData {
        categories = categories == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(categories));
        tasks = tasks == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    /**
     * Convenience constructor defaulting to the current schema version.
     *
     * @param categories categories list
     * @param tasks      tasks list
     */
    public AppData(List<Category> categories, List<Task> tasks) {
        this(CURRENT_SCHEMA_VERSION, categories, tasks);
    }

    /**
     * Creates an empty AppData instance pre-populated with the default "General" category.
     *
     * @return an empty AppData instance with default category
     */
    public static AppData empty() {
        return new AppData(CURRENT_SCHEMA_VERSION, List.of(Category.createDefault()), List.of());
    }
}
