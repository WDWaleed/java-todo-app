package com.todoapp.storage;

import com.todoapp.model.Category;
import com.todoapp.model.Task;
import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;

import java.util.List;
import java.util.Objects;

/**
 * Utility helper facilitating bidirectional synchronization between domain repositories
 * and immutable persistent {@link AppData} snapshots.
 */
public final class DataSync {

    private DataSync() {
        // utility class
    }

    /**
     * Clears and populates the given repositories with data from the specified snapshot.
     *
     * @param taskRepo     non-null task repository
     * @param categoryRepo non-null category repository
     * @param data         data snapshot to load (if null, repositories remain unchanged)
     */
    public static void loadInto(TaskRepository taskRepo, CategoryRepository categoryRepo, AppData data) {
        Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
        Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");
        if (data == null) {
            return;
        }

        categoryRepo.clear();
        for (Category category : data.categories()) {
            categoryRepo.save(category);
        }

        taskRepo.clear();
        for (Task task : data.tasks()) {
            taskRepo.save(task);
        }
    }

    /**
     * Overloaded convenience method accepting category repository first.
     *
     * @param categoryRepo non-null category repository
     * @param taskRepo     non-null task repository
     * @param data         data snapshot to load
     */
    public static void loadInto(CategoryRepository categoryRepo, TaskRepository taskRepo, AppData data) {
        loadInto(taskRepo, categoryRepo, data);
    }

    /**
     * Captures a consistent snapshot of the current state of both repositories.
     *
     * @param taskRepo     non-null task repository
     * @param categoryRepo non-null category repository
     * @return current AppData snapshot
     */
    public static AppData snapshot(TaskRepository taskRepo, CategoryRepository categoryRepo) {
        Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
        Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");

        List<Category> categories = categoryRepo.findAll();
        List<Task> tasks = taskRepo.findAll();
        return new AppData(AppData.CURRENT_SCHEMA_VERSION, categories, tasks);
    }

    /**
     * Overloaded convenience method accepting category repository first.
     *
     * @param categoryRepo non-null category repository
     * @param taskRepo     non-null task repository
     * @return current AppData snapshot
     */
    public static AppData snapshot(CategoryRepository categoryRepo, TaskRepository taskRepo) {
        return snapshot(taskRepo, categoryRepo);
    }
}
