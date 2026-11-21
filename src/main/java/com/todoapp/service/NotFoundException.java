package com.todoapp.service;

/**
 * Unchecked exception indicating that a requested domain entity was not found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a NotFoundException with an explanatory message.
     *
     * @param message error explanation
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a NotFoundException with an explanatory message and root cause.
     *
     * @param message error explanation
     * @param cause   the root cause exception
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
