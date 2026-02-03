package com.example.chronos.exception;

/**
 * Exception indicating a conflict (HTTP 409) e.g., unique constraint violation.
 */
public class ConflictException extends ApplicationException {

    public ConflictException() {
        super();
    }

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
