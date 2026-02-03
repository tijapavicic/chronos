package com.example.chronos.exception;

/**
 * Exception indicating access is forbidden (HTTP 403).
 */
public class ForbiddenException extends ApplicationException {

    public ForbiddenException() {
        super();
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
