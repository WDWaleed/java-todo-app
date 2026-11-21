package com.todoapp.storage;

/**
 * Checked exception thrown when storage persistence operations fail.
 */
public class StorageException extends Exception {

    /**
     * Constructs a StorageException with the specified descriptive message.
     *
     * @param message descriptive failure explanation
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Constructs a StorageException with the specified descriptive message and root cause.
     *
     * @param message descriptive failure explanation
     * @param cause   underlying root cause exception
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
