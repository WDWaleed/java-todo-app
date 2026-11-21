package com.todoapp.storage;

import com.todoapp.repository.CategoryRepository;
import com.todoapp.repository.TaskRepository;
import com.todoapp.service.ChangeType;
import com.todoapp.service.TaskChangeListener;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Observer that automatically schedules debounced persistence saves whenever tasks or categories change.
 * Buffers rapid subsequent mutations into a single disk write and flushes pending writes on shutdown.
 */
public class AutosaveListener implements TaskChangeListener, AutoCloseable {

    /**
     * Default debounce delay in milliseconds.
     */
    public static final long DEFAULT_DEBOUNCE_MS = 300L;

    private final Storage storage;
    private final TaskRepository taskRepo;
    private final CategoryRepository categoryRepo;
    private final ScheduledExecutorService executor;
    private final Consumer<String> errorCallback;
    private final long debounceDelayMs;
    private final boolean ownsExecutor;

    private final Object scheduleLock = new Object();
    private final Object saveLock = new Object();
    private ScheduledFuture<?> pendingTask;
    private boolean closed;

    /**
     * Full constructor allowing custom executor, error callback, and debounce delay.
     *
     * @param storage         non-null persistence storage target
     * @param taskRepo        non-null task repository
     * @param categoryRepo    non-null category repository
     * @param executor        non-null scheduled executor service
     * @param errorCallback   consumer invoked if background save fails (may be null)
     * @param debounceDelayMs debounce delay in milliseconds
     */
    public AutosaveListener(Storage storage,
                            TaskRepository taskRepo,
                            CategoryRepository categoryRepo,
                            ScheduledExecutorService executor,
                            Consumer<String> errorCallback,
                            long debounceDelayMs) {
        this.storage = Objects.requireNonNull(storage, "Storage must not be null");
        this.taskRepo = Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
        this.categoryRepo = Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");
        this.executor = Objects.requireNonNull(executor, "Executor must not be null");
        this.errorCallback = errorCallback != null ? errorCallback : msg -> {};
        this.debounceDelayMs = debounceDelayMs;
        this.ownsExecutor = false;
    }

    /**
     * Constructor creating an internal daemon scheduled executor and default debounce delay.
     *
     * @param storage       non-null persistence storage target
     * @param taskRepo      non-null task repository
     * @param categoryRepo  non-null category repository
     * @param errorCallback consumer invoked if background save fails (may be null)
     */
    public AutosaveListener(Storage storage,
                            TaskRepository taskRepo,
                            CategoryRepository categoryRepo,
                            Consumer<String> errorCallback) {
        this.storage = Objects.requireNonNull(storage, "Storage must not be null");
        this.taskRepo = Objects.requireNonNull(taskRepo, "TaskRepository must not be null");
        this.categoryRepo = Objects.requireNonNull(categoryRepo, "CategoryRepository must not be null");
        this.errorCallback = errorCallback != null ? errorCallback : msg -> {};
        this.debounceDelayMs = DEFAULT_DEBOUNCE_MS;
        this.ownsExecutor = true;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "autosave-thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Convenience constructor without error callback using an internal daemon executor.
     *
     * @param storage      non-null persistence storage target
     * @param taskRepo     non-null task repository
     * @param categoryRepo non-null category repository
     */
    public AutosaveListener(Storage storage,
                            TaskRepository taskRepo,
                            CategoryRepository categoryRepo) {
        this(storage, taskRepo, categoryRepo, (Consumer<String>) null);
    }

    /**
     * Convenience constructor with custom executor and default debounce.
     *
     * @param storage      non-null persistence storage target
     * @param taskRepo     non-null task repository
     * @param categoryRepo non-null category repository
     * @param executor     non-null scheduled executor service
     */
    public AutosaveListener(Storage storage,
                            TaskRepository taskRepo,
                            CategoryRepository categoryRepo,
                            ScheduledExecutorService executor) {
        this(storage, taskRepo, categoryRepo, executor, null, DEFAULT_DEBOUNCE_MS);
    }

    @Override
    public void onTasksChanged(ChangeType type, UUID taskId) {
        synchronized (scheduleLock) {
            if (closed) {
                return;
            }
            if (pendingTask != null && !pendingTask.isDone()) {
                pendingTask.cancel(false);
            }
            pendingTask = executor.schedule(this::executeSave, debounceDelayMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Flushes any pending autosave immediately and synchronously on the current thread.
     */
    public void flush() {
        synchronized (scheduleLock) {
            if (pendingTask != null) {
                pendingTask.cancel(false);
                pendingTask = null;
            }
        }
        executeSave();
    }

    private void executeSave() {
        synchronized (saveLock) {
            try {
                AppData snapshot = DataSync.snapshot(taskRepo, categoryRepo);
                storage.save(snapshot);
            } catch (Exception e) {
                if (errorCallback != null) {
                    try {
                        errorCallback.accept("Failed to autosave: " + e.getMessage());
                    } catch (Exception ignored) {
                        // Suppress exception thrown inside user callback
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        synchronized (scheduleLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        flush();
        if (ownsExecutor) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }
}
