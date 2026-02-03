package com.example.chronos.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationExceptionTest {

    @Test
    void builderBuildsInstance() {
        // Lombok's @Builder should generate a builder even if there are no explicit fields.
        ApplicationException ex = ApplicationException.builder().build();
        assertNotNull(ex, "builder().build() should return an instance");
        assertNull(ex.getMessage(), "default message should be null");
        assertNull(ex.getCause(), "default cause should be null");
        assertTrue(ex instanceof RuntimeException, "ApplicationException should extend RuntimeException");
    }

    @Test
    void protectedConstructorsSetMessageAndCause() {
        Throwable cause = new RuntimeException("root");

        // protected constructors are accessible within the same package
        ApplicationException e1 = new ApplicationException("hello");
        assertEquals("hello", e1.getMessage());
        assertNull(e1.getCause());

        ApplicationException e2 = new ApplicationException("hello2", cause);
        assertEquals("hello2", e2.getMessage());
        assertSame(cause, e2.getCause());
    }
}
