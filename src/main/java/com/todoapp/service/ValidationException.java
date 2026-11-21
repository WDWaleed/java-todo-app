package com.todoapp.service;

/**
 * Unchecked exception indicating a business validation violation with a user-facing message.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a ValidationException with a user-facing message.
     *
     * @param message user-friendly error explanation
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a ValidationException with a user-facing message and underlying cause.
     *
     * @param message user-friendly error explanation
     * @param cause   the root cause exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
