package com.todoapp.repository;

import com.todoapp.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage-agnostic repository interface defining CRUD operations for {@link Task} entities.
 * <p>
 * Implementations of this interface store and return entity instances directly.
 * Defensive copying and immutability guarantees are the responsibility of the service layer.
 * </p>
 */
public interface TaskRepository {

    /**
     * Returns all tasks in persistent or in-memory storage.
     *
     * @return unmodifiable or ordered list of tasks
     */
    List<Task> findAll();

    /**
     * Finds a task by its unique identifier.
     *
     * @param id non-null task UUID
     * @return Optional containing matching task, or empty if not found
     */
    Optional<Task> findById(UUID id);

    /**
     * Inserts or updates a task in the repository.
     *
     * @param task non-null task to persist
     * @return the saved task instance
     */
    Task save(Task task);

    /**
     * Deletes a task by its unique identifier.
     *
     * @param id non-null task UUID
     * @return Optional containing the deleted task, or empty if no task matched
     */
    Optional<Task> deleteById(UUID id);

    /**
     * Clears all stored tasks.
     */
    void clear();

    /**
     * Returns the total count of stored tasks.
     *
     * @return total task count
     */
    int count();
}
