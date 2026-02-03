package com.example.chronos.exception;

import java.time.Duration;

/**
 * Exception that signals a database timeout (for example, query timeout or connection timeout).
 * Includes an optional timeout duration to allow mapping to different HTTP statuses
 * (e.g., 408 Request Timeout for short timeouts, 503 Service Unavailable for longer ones).
 */
public class DatabaseTimeoutException extends ApplicationException {

    private final Duration timeout;

    public DatabaseTimeoutException() {
        super();
        this.timeout = null;
    }

    public DatabaseTimeoutException(String message) {
        super(message);
        this.timeout = null;
    }

    public DatabaseTimeoutException(String message, Throwable cause) {
        super(message, cause);
        this.timeout = null;
    }

    public DatabaseTimeoutException(String message, Duration timeout) {
        super(message);
        this.timeout = timeout;
    }

    public DatabaseTimeoutException(String message, Duration timeout, Throwable cause) {
        super(message, cause);
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
