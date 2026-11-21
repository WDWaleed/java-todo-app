package com.todoapp.repository;

import com.todoapp.model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of {@link TaskRepository} backed by a {@link LinkedHashMap}
 * to guarantee $O(1)$ primary key access and deterministic insertion order.
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<UUID, Task> tasks = new LinkedHashMap<>();

    /**
     * Constructs an empty in-memory task repository.
     */
    public InMemoryTaskRepository() {
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<Task> findById(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public Task save(Task task) {
        Objects.requireNonNull(task, "Task to save must not be null");
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> deleteById(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(tasks.remove(id));
    }

    @Override
    public void clear() {
        tasks.clear();
    }

    @Override
    public int count() {
        return tasks.size();
    }
}
