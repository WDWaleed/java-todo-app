package com.todoapp.repository;

import com.todoapp.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage-agnostic repository interface defining data access operations for {@link Category} entities.
 * <p>
 * Implementations of this interface store and return entity instances directly.
 * The default "General" category is guaranteed to be present at all times.
 * </p>
 */
public interface CategoryRepository {

    /**
     * Returns all categories in deterministic order.
     *
     * @return list of stored categories
     */
    List<Category> findAll();

    /**
     * Finds a category by its unique identifier.
     *
     * @param id non-null category UUID
     * @return Optional containing category, or empty if not found
     */
    Optional<Category> findById(UUID id);

    /**
     * Finds a category matching the given name, ignoring case.
     *
     * @param name non-null name string
     * @return Optional containing matching category, or empty if not found
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Inserts or updates a category in the repository.
     *
     * @param category non-null category to save
     * @return the saved category instance
     */
    Category save(Category category);

    /**
     * Deletes a category by its unique identifier.
     * Note: Implementations must not allow deletion of the default category.
     *
     * @param id non-null category UUID
     * @return Optional containing deleted category, or empty if not found or cannot be deleted
     */
    Optional<Category> deleteById(UUID id);

    /**
     * Clears custom categories and resets repository to default state.
     */
    void clear();

    /**
     * Returns the count of all categories (including the default category).
     *
     * @return total category count
     */
    int count();
}
