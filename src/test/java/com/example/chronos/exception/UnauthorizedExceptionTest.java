package com.example.chronos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnauthorizedException Tests")
class UnauthorizedExceptionTest {

    @Test
    @DisplayName("Constructor with message should create exception")
    void constructorWithMessage() {
        // Given
        String message = "Unauthorized access";

        // When
        UnauthorizedException exception = new UnauthorizedException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message and cause should create exception")
    void constructorWithMessageAndCause() {
        // Given
        String message = "Unauthorized access";
        Throwable cause = new SecurityException("Invalid token");

        // When
        UnauthorizedException exception = new UnauthorizedException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // Given / When
        UnauthorizedException exception = new UnauthorizedException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
