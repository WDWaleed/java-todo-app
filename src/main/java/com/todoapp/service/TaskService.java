package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskFactory;
import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing task business logic, lifecycle workflows, validations,
 * undo buffers, and change notification dispatching.
 * <p>
 * Immutability Boundary: Public methods consistently return deep copies (via {@link Task#copy()})
 * so callers cannot mutate repository state directly.
 * </p>
 */
public class TaskService {

    private static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());

    private final TaskRepository taskRepo;
    private final CategoryRepository categoryRepo;
    private final Clock clock;
    private final List<TaskChangeListener> listeners = new CopyOnWriteArrayList<>();
    private Task undoBuffer;

    /**
     * Constructs TaskService with repository dependencies and an injected Clock.
     *
     * @param taskRepo     task repository abstraction
     * @param categoryRepo category repository abstraction
     * @param clock        clock used for deterministic timestamps
     */
    public TaskService(TaskRepository taskRepo, CategoryRepository categoryRepo, Clock clock) {
        this.taskRepo = Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
        this.categoryRepo = Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Convenience constructor using the system default clock.
     *
     * @param taskRepo     task repository abstraction
     * @param categoryRepo category repository abstraction
     */
    public TaskService(TaskRepository taskRepo, CategoryRepository categoryRepo) {
        this(taskRepo, categoryRepo, Clock.systemDefaultZone());
    }

    /**
     * Creates and persists a new task based on the provided draft.
     *
     * @param draft validated task draft
     * @return deep copy of the created and saved Task
     * @throws ValidationException if draft constraints or category reference fails
     */
    public Task create(TaskDraft draft) {
        validateDraft(draft);

        Task task;
        try {
            task = TaskFactory.create(draft);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ex.getMessage(), ex);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        task.touch(now);
        taskRepo.save(task);

        notifyListeners(ChangeType.CREATED, task.getId());
        return task.copy();
    }

    /**
     * Retrieves a task by its identifier.
     *
     * @param id task UUID
     * @return deep copy of the retrieved task
     * @throws NotFoundException if task does not exist
     */
    public Task get(UUID id) {
        if (id == null) {
            throw new NotFoundException("Task ID must not be null");
        }
        return taskRepo.findById(id)
                .map(Task::copy)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));
    }

    /**
     * Returns all stored tasks as defensive copies.
     *
     * @return list of task copies
     */
    public List<Task> getAll() {
        List<Task> stored = taskRepo.findAll();
        List<Task> copies = new ArrayList<>(stored.size());
        for (Task t : stored) {
            copies.add(t.copy());
        }
        return copies;
    }

    /**
     * Updates an existing task using attributes from a draft.
     * The polymorphic task type cannot be modified.
     *
     * @param id    target task UUID
     * @param draft draft containing modified attributes
     * @return deep copy of the updated task
     * @throws ValidationException if task type changes, validation fails, or category missing
     * @throws NotFoundException   if task does not exist
     */
    public Task update(UUID id, TaskDraft draft) {
        if (id == null) {
            throw new NotFoundException("Task ID must not be null");
        }
        Task existing = taskRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        validateDraft(draft);

        if (existing.getType() != draft.type()) {
            throw new ValidationException("Task type cannot be changed from "
                    + existing.getType() + " to " + draft.type());
        }

        try {
            TaskFactory.apply(existing, draft);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ex.getMessage(), ex);
        }

        existing.touch(LocalDateTime.now(clock));
        taskRepo.save(existing);

        notifyListeners(ChangeType.UPDATED, existing.getId());
        return existing.copy();
    }

    // ==========================================
    // Internal Helpers
    // ==========================================

    private void validateDraft(TaskDraft draft) {
        if (draft == null) {
            throw new ValidationException("Task draft must not be null");
        }
        try {
            draft.validate();
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ex.getMessage(), ex);
        }

        // Verify referenced category exists
        if (draft.categoryId() != null && categoryRepo.findById(draft.categoryId()).isEmpty()) {
            throw new ValidationException("Referenced category does not exist: " + draft.categoryId());
        }
    }

    private void notifyListeners(ChangeType type, UUID taskId) {
        for (TaskChangeListener listener : listeners) {
            try {
                listener.onTasksChanged(type, taskId);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error executing TaskChangeListener", ex);
            }
        }
    }
}
