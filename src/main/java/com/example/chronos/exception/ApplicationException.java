package com.example.chronos.exception;

import lombok.Builder;

@Builder
public class ApplicationException extends RuntimeException{
    protected ApplicationException() {
        super();
    }

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
