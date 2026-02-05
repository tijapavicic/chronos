package com.example.chronos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DatabaseTimeoutException Tests")
class DatabaseTimeoutExceptionTest {

    @Test
    @DisplayName("Constructor with message should create exception with null timeout")
    void constructorWithMessage() {
        // Given
        String message = "Database timeout";

        // When
        DatabaseTimeoutException exception = new DatabaseTimeoutException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getTimeout()).isNull();
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message and timeout should create exception")
    void constructorWithMessageAndTimeout() {
        // Given
        String message = "Database timeout after 5 seconds";
        Duration timeout = Duration.ofSeconds(5);

        // When
        DatabaseTimeoutException exception = new DatabaseTimeoutException(message, timeout);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getTimeout()).isEqualTo(timeout);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message, timeout and cause should create exception")
    void constructorWithMessageTimeoutAndCause() {
        // Given
        String message = "Database timeout";
        Duration timeout = Duration.ofMinutes(2);
        Throwable cause = new RuntimeException("Connection lost");

        // When
        DatabaseTimeoutException exception = new DatabaseTimeoutException(message, timeout, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getTimeout()).isEqualTo(timeout);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Should support short timeout duration")
    void shouldSupportShortTimeout() {
        // Given
        Duration shortTimeout = Duration.ofMillis(500);

        // When
        DatabaseTimeoutException exception = new DatabaseTimeoutException("Short timeout", shortTimeout);

        // Then
        assertThat(exception.getTimeout()).isEqualTo(shortTimeout);
        assertThat(exception.getTimeout().toMillis()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should support long timeout duration")
    void shouldSupportLongTimeout() {
        // Given
        Duration longTimeout = Duration.ofMinutes(10);

        // When
        DatabaseTimeoutException exception = new DatabaseTimeoutException("Long timeout", longTimeout);

        // Then
        assertThat(exception.getTimeout()).isEqualTo(longTimeout);
        assertThat(exception.getTimeout().toMinutes()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // Given / When
        DatabaseTimeoutException exception = new DatabaseTimeoutException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
