package com.example.chronos.controller;

import com.example.chronos.config.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalControllerAdvice Unit Tests")
class GlobalControllerAdviceTest {

    private GlobalControllerAdvice advice;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        advice = new GlobalControllerAdvice();
    }

    @Test
    @DisplayName("handleAccessDenied - should return 403 with error response")
    void handleAccessDenied_Returns403() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-123");

        // When
        var response = advice.handleAccessDenied(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_FORBIDDEN");
        assertThat(response.getBody().getCorrelationId()).isEqualTo("corr-123");
    }

    @Test
    @DisplayName("handleAuthentication - should return 401 with error response")
    void handleAuthentication_Returns401() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-456");

        // When
        var response = advice.handleAuthentication(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Bad credentials");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_UNAUTHORIZED");
        assertThat(response.getBody().getCorrelationId()).isEqualTo("corr-456");
    }

    @Test
    @DisplayName("handleConstraintViolation - should return 400 with validation errors")
    void handleConstraintViolation_Returns400() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("must not be blank");
        when(violation2.getMessage()).thenReturn("size must be between 1 and 100");

        Set<ConstraintViolation<?>> violations = new HashSet<>(Arrays.asList(violation1, violation2));
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        when(request.getRequestURI()).thenReturn("/api/validate");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-789");

        // When
        var response = advice.handleConstraintViolation(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_VALIDATION");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).containsExactlyInAnyOrder("must not be blank", "size must be between 1 and 100");
    }

    @Test
    @DisplayName("handleTypeMismatch - should return 400 with parameter error")
    void handleTypeMismatch_Returns400() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("facilityId");
        when(exception.getValue()).thenReturn("not-a-uuid");

        when(request.getRequestURI()).thenReturn("/api/facilities/not-a-uuid");
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("corr-abc");

        // When
        var response = advice.handleTypeMismatch(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("facilityId");
        assertThat(response.getBody().getMessage()).contains("not-a-uuid");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_BAD_REQUEST");
    }

    @Test
    @DisplayName("handleValidation - should return 400 with field errors")
    void handleValidation_Returns400() {
        // Given
        FieldError error1 = new FieldError("facilityDTO", "facilityName", "must not be blank");
        FieldError error2 = new FieldError("facilityDTO", "facilityType", "must not be null");

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));

        when(request.getRequestURI()).thenReturn("/api/facilities");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-def");

        // When
        var response = advice.handleValidation(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_VALIDATION");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).anyMatch(msg -> msg.contains("facilityName"));
        assertThat(response.getBody().getDetails()).anyMatch(msg -> msg.contains("facilityType"));
    }

    @Test
    @DisplayName("handleUnreadable - should return 400 with message from most specific cause")
    void handleUnreadable_Returns400() {
        // Given
        Throwable cause = new IllegalArgumentException("Invalid JSON format");
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMostSpecificCause()).thenReturn(cause);

        when(request.getRequestURI()).thenReturn("/api/facilities");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-ghi");

        // When
        var response = advice.handleUnreadable(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid JSON format");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_BAD_PAYLOAD");
    }

    @Test
    @DisplayName("handleGeneric - should return 500 for unexpected exceptions")
    void handleGeneric_Returns500() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("corr-jkl");

        // When
        var response = advice.handleGeneric(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error");
        assertThat(response.getBody().getCode()).isEqualTo("ERR_INTERNAL");
    }

    @Test
    @DisplayName("getCorrelationId - should return id from request attribute")
    void getCorrelationId_FromAttribute() {
        // Given
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("attr-123");

        // When
        String result = advice.getCorrelationId(request);

        // Then
        assertThat(result).isEqualTo("attr-123");
    }

    @Test
    @DisplayName("getCorrelationId - should return id from header if attribute is blank")
    void getCorrelationId_FromHeader() {
        // Given
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn("");
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("header-456");

        // When
        String result = advice.getCorrelationId(request);

        // Then
        assertThat(result).isEqualTo("header-456");
    }

    @Test
    @DisplayName("getCorrelationId - should return null if no correlation id present")
    void getCorrelationId_Null() {
        // Given
        when(request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR)).thenReturn(null);
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        // When
        String result = advice.getCorrelationId(request);

        // Then
        assertThat(result).isNull();
    }
}
