package com.example.chronos.controller;

import com.example.chronos.dto.ErrorResponse;
import com.example.chronos.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class FacilityControllerAdvice extends GlobalControllerAdvice{

    private static final Map<Class<? extends ApplicationException>, HttpStatus> STATUS_MAP =
            new LinkedHashMap<>();
    static {
        // ordered: most specific -> more generic
        STATUS_MAP.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
        STATUS_MAP.put(BadRequestException.class, HttpStatus.BAD_REQUEST);
        STATUS_MAP.put(ConflictException.class, HttpStatus.CONFLICT);
        STATUS_MAP.put(UnauthorizedException.class, HttpStatus.UNAUTHORIZED);
        STATUS_MAP.put(ForbiddenException.class, HttpStatus.FORBIDDEN);
        // DatabaseTimeoutException handled specially below (based on timeout duration)
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleApplicationException(
            ApplicationException ex,
            HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        HttpStatus status = mapStatus(ex);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("ERR_APPLICATION")
                .correlationId(correlationId)
                .build();
        return new ResponseEntity<>(body, status);
    }

    private HttpStatus mapStatus(ApplicationException exception){
        // Special-case DatabaseTimeoutException:
        // choose 408 for short timeouts (<3 minutes), otherwise 503
        if (exception instanceof DatabaseTimeoutException) {
            DatabaseTimeoutException dte = (DatabaseTimeoutException) exception;
            Duration t = dte.getTimeout();
            if (t != null && t.compareTo(Duration.ofMinutes(3)) < 0) {
                return HttpStatus.REQUEST_TIMEOUT; // 408
            } else {
                return HttpStatus.SERVICE_UNAVAILABLE; // 503
            }
        }

        for (Map.Entry<Class<? extends ApplicationException>, HttpStatus> entry : STATUS_MAP.entrySet()) {
            if (entry.getKey().isInstance(exception)) {
                return entry.getValue();
            }
        }
        return HttpStatus.BAD_REQUEST;
    }
}
