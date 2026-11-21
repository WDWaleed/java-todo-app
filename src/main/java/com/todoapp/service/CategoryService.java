package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.Task;
import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service orchestrating category domain operations, validation rules,
 * unique constraints, task reassignment on deletion, and change notifications.
 */
public class CategoryService {

    private static final Logger LOGGER = Logger.getLogger(CategoryService.class.getName());

    private final CategoryRepository categoryRepo;
    private final TaskRepository taskRepo;
    private final List<TaskChangeListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs the CategoryService with required repository dependencies.
     *
     * @param categoryRepo category repository abstraction
     * @param taskRepo     task repository abstraction
     */
    public CategoryService(CategoryRepository categoryRepo, TaskRepository taskRepo) {
        this.categoryRepo = Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");
        this.taskRepo = Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
    }

    /**
     * Registers a listener for category and task change events.
     *
     * @param listener observer to register
     */
    public void addListener(TaskChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void notifyListeners(ChangeType type, UUID id) {
        for (TaskChangeListener listener : listeners) {
            try {
                listener.onTasksChanged(type, id);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error executing TaskChangeListener", ex);
            }
        }
    }

    /**
     * Creates a new category with uniqueness and format validations.
     *
     * @param name     category name (1–30 characters)
     * @param colorHex hex color string (format #RRGGBB)
     * @return the created Category
     * @throws ValidationException if name is blank, duplicate, or color is invalid
     */
    public Category create(String name, String colorHex) {
        String trimmedName = validateAndTrimName(name);
        validateUniqueName(trimmedName, null);
        validateColorHex(colorHex);

        Category category = new Category(trimmedName, colorHex.trim().toUpperCase());
        categoryRepo.save(category);
        notifyListeners(ChangeType.CATEGORY_CHANGED, category.getId());
        return category;
    }

    /**
     * Renames an existing category. The default "General" category cannot be renamed.
     *
     * @param id      category UUID
     * @param newName new name (1–30 characters)
     * @return the updated Category
     * @throws ValidationException if category is default, name is invalid or duplicate
     * @throws NotFoundException   if category does not exist
     */
    public Category rename(UUID id, String newName) {
        Objects.requireNonNull(id, "Category ID must not be null");
        if (Category.DEFAULT_ID.equals(id)) {
            throw new ValidationException("The default 'General' category cannot be renamed");
        }

        Category existing = get(id);
        String trimmedName = validateAndTrimName(newName);
        validateUniqueName(trimmedName, id);

        existing.setName(trimmedName);
        categoryRepo.save(existing);
        notifyListeners(ChangeType.CATEGORY_CHANGED, id);
        return existing;
    }

    /**
     * Updates the color code of an existing category.
     *
     * @param id          category UUID
     * @param newColorHex new hex color string (#RRGGBB)
     * @return the updated Category
     * @throws ValidationException if color format is invalid
     * @throws NotFoundException   if category does not exist
     */
    public Category recolor(UUID id, String newColorHex) {
        Objects.requireNonNull(id, "Category ID must not be null");
        Category existing = get(id);
        validateColorHex(newColorHex);

        existing.setColorHex(newColorHex.trim().toUpperCase());
        categoryRepo.save(existing);
        notifyListeners(ChangeType.CATEGORY_CHANGED, id);
        return existing;
    }

    /**
     * Deletes a category. The default "General" category cannot be deleted.
     * Any tasks associated with the deleted category are reassigned to "General".
     *
     * @param id category UUID to delete
     * @return the deleted Category
     * @throws ValidationException if attempting to delete the default category
     * @throws NotFoundException   if category does not exist
     */
    public Category delete(UUID id) {
        Objects.requireNonNull(id, "Category ID must not be null");
        if (Category.DEFAULT_ID.equals(id)) {
            throw new ValidationException("The default 'General' category cannot be deleted");
        }

        Category existing = get(id);

        // Reassign all tasks belonging to this category to General
        List<Task> tasks = taskRepo.findAll();
        for (Task task : tasks) {
            if (id.equals(task.getCategoryId())) {
                task.setCategoryId(Category.DEFAULT_ID);
                task.touch();
                taskRepo.save(task);
            }
        }

        categoryRepo.deleteById(id);
        notifyListeners(ChangeType.CATEGORY_CHANGED, id);
        return existing;
    }

    /**
     * Returns all categories in deterministic order (default category first).
     *
     * @return list of categories
     */
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    /**
     * Retrieves a category by its identifier.
     *
     * @param id category UUID
     * @return Category instance
     * @throws NotFoundException if category does not exist
     */
    public Category get(UUID id) {
        if (id == null) {
            throw new NotFoundException("Category ID must not be null");
        }
        return categoryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + id));
    }

    /**
     * Returns the count of tasks currently assigned to the given category.
     *
     * @param categoryId category UUID
     * @return number of tasks
     */
    public int countTasks(UUID categoryId) {
        if (categoryId == null) return 0;
        return (int) taskRepo.findAll().stream()
                .filter(t -> categoryId.equals(t.getCategoryId()))
                .count();
    }

    // ==========================================
    // Internal Validation Helpers
    // ==========================================

    private String validateAndTrimName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() > Category.MAX_NAME_LENGTH) {
            throw new ValidationException("Category name must not exceed " + Category.MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    private void validateUniqueName(String name, UUID excludeId) {
        Optional<Category> existing = categoryRepo.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            if (excludeId == null || !existing.get().getId().equals(excludeId)) {
                throw new ValidationException("A category named '" + name + "' already exists");
            }
        }
    }

    private void validateColorHex(String colorHex) {
        if (colorHex == null || !Category.COLOR_PATTERN.matcher(colorHex.trim()).matches()) {
            throw new ValidationException("Invalid color hex format: '" + colorHex + "'. Expected format: #RRGGBB");
        }
    }
}
