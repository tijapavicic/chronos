package com.example.chronos.exception;

/**
 * Exception indicating an authentication required or failed (HTTP 401).
 */
public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
