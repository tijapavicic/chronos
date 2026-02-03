package com.example.chronos.exception;

/**
 * Exception indicating a client-side bad request (HTTP 400).
 */
public class BadRequestException extends ApplicationException {

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
