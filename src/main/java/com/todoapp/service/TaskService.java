package com.todoapp.service;

import com.todoapp.model.Category;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.model.TaskDraft;
import com.todoapp.model.TaskFactory;
import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
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
     * Registers a listener for task mutation events.
     *
     * @param listener observer to register
     */
    public void addListener(TaskChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a previously registered listener.
     *
     * @param listener observer to remove
     */
    public void removeListener(TaskChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
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

    /**
     * Deletes a task by ID, placing a deep copy into the single-slot undo buffer.
     *
     * @param id task UUID to delete
     * @return deep copy of the deleted task
     * @throws NotFoundException if task does not exist
     */
    public Task delete(UUID id) {
        if (id == null) {
            throw new NotFoundException("Task ID must not be null");
        }
        Task deleted = taskRepo.deleteById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        this.undoBuffer = deleted.copy();
        notifyListeners(ChangeType.DELETED, id);
        return deleted.copy();
    }

    /**
     * Restores the task currently stored in the undo buffer into the repository.
     * The undo buffer is cleared once restored.
     *
     * @return Optional containing the restored task, or empty if undo buffer is vacant
     */
    public Optional<Task> undoDelete() {
        if (undoBuffer == null) {
            return Optional.empty();
        }
        Task toRestore = undoBuffer.copy();
        undoBuffer = null;

        toRestore.touch(LocalDateTime.now(clock));
        taskRepo.save(toRestore);

        notifyListeners(ChangeType.RESTORED, toRestore.getId());
        return Optional.of(toRestore.copy());
    }

    /**
     * Checks if there is a deleted task in the single-slot undo buffer ready to be restored.
     *
     * @return true if undo buffer contains an entity, false otherwise
     */
    public boolean canUndo() {
        return undoBuffer != null;
    }

    /**
     * Directly restores a task instance (e.g. for undo workflows).
     *
     * @param task task to restore
     * @return deep copy of the restored task
     */
    public Task restore(Task task) {
        Objects.requireNonNull(task, "Task to restore must not be null");
        Task copy = task.copy();
        copy.touch(LocalDateTime.now(clock));
        taskRepo.save(copy);
        notifyListeners(ChangeType.RESTORED, copy.getId());
        return copy.copy();
    }

    /**
     * Toggles the completion state of a task.
     *
     * @param id target task UUID
     * @return deep copy of the modified task
     * @throws NotFoundException if task does not exist
     */
    public Task toggleComplete(UUID id) {
        Task task = getEntity(id);
        task.setCompleted(!task.isCompleted());
        task.touch(LocalDateTime.now(clock));
        taskRepo.save(task);

        notifyListeners(ChangeType.UPDATED, id);
        return task.copy();
    }

    /**
     * Updates the priority level of a task.
     *
     * @param id       target task UUID
     * @param priority non-null priority level
     * @return deep copy of the modified task
     * @throws ValidationException if priority is null
     * @throws NotFoundException   if task does not exist
     */
    public Task setPriority(UUID id, Priority priority) {
        if (priority == null) {
            throw new ValidationException("Priority must not be null");
        }
        Task task = getEntity(id);
        task.setPriority(priority);
        task.touch(LocalDateTime.now(clock));
        taskRepo.save(task);

        notifyListeners(ChangeType.UPDATED, id);
        return task.copy();
    }

    /**
     * Toggles the pinned status of a task.
     *
     * @param id target task UUID
     * @return deep copy of the modified task
     * @throws NotFoundException if task does not exist
     */
    public Task togglePinned(UUID id) {
        Task task = getEntity(id);
        task.setPinned(!task.isPinned());
        task.touch(LocalDateTime.now(clock));
        taskRepo.save(task);

        notifyListeners(ChangeType.UPDATED, id);
        return task.copy();
    }

    /**
     * Queries and sorts tasks matching composite filtering, searching, and sorting criteria.
     * Guaranteed invariants:
     * - Pinned tasks always precede unpinned tasks.
     * - Returns new list of defensive task copies.
     * - Stable tiebreakers: {@code createdAt} followed by {@code id}.
     *
     * @param query query criteria (defaults to {@link TaskQuery#defaultQuery()} if null)
     * @return filtered, sorted list of task copies
     */
    public List<Task> query(TaskQuery query) {
        TaskQuery q = query != null ? query : TaskQuery.defaultQuery();

        Predicate<Task> predicate = buildPredicate(q);

        Comparator<Task> fullComparator = Comparator.comparing(Task::isPinned).reversed()
                .thenComparing(q.sort().getComparator(q.ascending()))
                .thenComparing(Task::getCreatedAt)
                .thenComparing(Task::getId);

        return taskRepo.findAll().stream()
                .filter(predicate)
                .sorted(fullComparator)
                .map(Task::copy)
                .toList();
    }

    /**
     * Counts tasks per priority level based on a base query.
     * Computes the distribution across High, Medium, and Low priorities.
     *
     * @param base base query filters (category, status, search)
     * @return EnumMap mapping each Priority to its matching task count
     */
    public Map<Priority, Integer> countByPriority(TaskQuery base) {
        TaskQuery baseQuery = base != null ? base : TaskQuery.defaultQuery();
        TaskQuery queryWithoutPriority = baseQuery.withPriority(null);

        Predicate<Task> predicate = buildPredicate(queryWithoutPriority);
        Map<Priority, Integer> counts = new EnumMap<>(Priority.class);
        for (Priority p : Priority.values()) {
            counts.put(p, 0);
        }

        for (Task task : taskRepo.findAll()) {
            if (predicate.test(task)) {
                counts.put(task.getPriority(), counts.get(task.getPriority()) + 1);
            }
        }
        return counts;
    }

    /**
     * Returns the total count of active (uncompleted) tasks.
     *
     * @return active task count
     */
    public int countActive() {
        return (int) taskRepo.findAll().stream()
                .filter(t -> !t.isCompleted())
                .count();
    }

    /**
     * Returns the total count of completed tasks.
     *
     * @return completed task count
     */
    public int countCompleted() {
        return (int) taskRepo.findAll().stream()
                .filter(Task::isCompleted)
                .count();
    }

    private Predicate<Task> buildPredicate(TaskQuery q) {
        Predicate<Task> predicate = t -> true;

        // Category filter
        if (q.categoryId() != null) {
            predicate = predicate.and(t -> q.categoryId().equals(t.getCategoryId()));
        }

        // Status filter
        if (q.status() == StatusFilter.ACTIVE) {
            predicate = predicate.and(t -> !t.isCompleted());
        } else if (q.status() == StatusFilter.COMPLETED) {
            predicate = predicate.and(Task::isCompleted);
        }

        // Priority filter
        if (q.priority() != null) {
            predicate = predicate.and(t -> t.getPriority() == q.priority());
        }

        // Search text
        String search = q.searchText() != null ? q.searchText().trim().toLowerCase(Locale.ROOT) : "";
        if (!search.isEmpty()) {
            predicate = predicate.and(t -> {
                String title = t.getTitle().toLowerCase(Locale.ROOT);
                String desc = t.getDescription().toLowerCase(Locale.ROOT);
                return title.contains(search) || desc.contains(search);
            });
        }

        return predicate;
    }

    private Task getEntity(UUID id) {
        if (id == null) {
            throw new NotFoundException("Task ID must not be null");
        }
        return taskRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));
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
