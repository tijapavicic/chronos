package com.example.chronos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorrelationIdFilter Unit Tests")
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("doFilter - should use correlation id from request header if present")
    void doFilter_UsesExistingCorrelationId() throws IOException, ServletException {
        // Given
        String existingId = "existing-correlation-id";
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(existingId);

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).setAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR, existingId);
        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);
        verify(filterChain).doFilter(request, response);

        // MDC should be cleared after filter chain
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_ATTR)).isNull();
    }

    @Test
    @DisplayName("doFilter - should generate correlation id if header is missing")
    void doFilter_GeneratesCorrelationId() throws IOException, ServletException {
        // Given
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).setAttribute(eq(CorrelationIdFilter.CORRELATION_ID_ATTR), anyString());
        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(filterChain).doFilter(request, response);

        // MDC should be cleared
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_ATTR)).isNull();
    }

    @Test
    @DisplayName("doFilter - should generate correlation id if header is blank")
    void doFilter_GeneratesCorrelationIdWhenBlank() throws IOException, ServletException {
        // Given
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("   ");

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(request).setAttribute(eq(CorrelationIdFilter.CORRELATION_ID_ATTR), anyString());
        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilter - should clean up MDC even if filter chain throws exception")
    void doFilter_CleansMdcOnException() throws IOException, ServletException {
        // Given
        String correlationId = "test-id";
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId);
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // When / Then
        try {
            filter.doFilter(request, response, filterChain);
        } catch (ServletException e) {
            // Expected exception
        }

        // MDC should be cleaned up even after exception
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_ATTR)).isNull();
    }

    @Test
    @DisplayName("Constants - should have correct header name")
    void constants_CorrectHeaderName() {
        assertThat(CorrelationIdFilter.CORRELATION_ID_HEADER).isEqualTo("X-Correlation-Id");
    }

    @Test
    @DisplayName("Constants - should have correct attribute name")
    void constants_CorrectAttributeName() {
        assertThat(CorrelationIdFilter.CORRELATION_ID_ATTR).isEqualTo("correlationId");
    }
}
