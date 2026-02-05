package com.example.chronos.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationExceptionTest {
    // small subtestclass because ApplicationException is abstract and cannot be instantiated directly
    static class TestException extends ApplicationException {
        public TestException() {
            super();
        }

        public TestException(String message) {
            super(message);
        }

        public TestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    void testDefaultConstructor() {
        TestException ex = new TestException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageConstructor() {
        String message = "Test message";
        TestException ex = new TestException(message);
        assertEquals(message, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Cause");
        TestException ex = new TestException(message, cause);
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
    @Test
    void testNullMessage() {
        TestException ex = new TestException((String) null);
        assertNull(ex.getMessage());
    }
    @Test
    void testNullCause() {
        TestException ex = new TestException("Test message", null);
        assertEquals("Test message", ex.getMessage());
        assertNull(ex.getCause());
    }
    @Test
    void testMessageWithNullCause() {
        TestException ex = new TestException("Test message", null);
        assertEquals("Test message", ex.getMessage());
        assertNull(ex.getCause());
    }


}
