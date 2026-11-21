package com.todoapp.storage;

/**
 * Storage abstraction responsible for loading and saving the application state snapshot.
 */
public interface Storage {

    /**
     * Loads the application state snapshot from persistent storage.
     *
     * @return restored application data
     * @throws StorageException if persistent storage access fails unrecoverably
     */
    AppData load() throws StorageException;

    /**
     * Persists the application state snapshot to persistent storage.
     *
     * @param data non-null application data snapshot to save
     * @throws StorageException if persistence fails
     */
    void save(AppData data) throws StorageException;
}
