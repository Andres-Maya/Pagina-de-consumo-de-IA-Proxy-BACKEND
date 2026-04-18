package com.aiproxy.presentation.exception;

import com.aiproxy.domain.exception.InsufficientTokensException;
import com.aiproxy.domain.exception.ThrottlingExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ThrottlingExceededException.class)
    public ResponseEntity<Map<String, Object>> handleThrottling(ThrottlingExceededException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Rate limit exceeded");
        body.put("message", ex.getMessage());
        body.put("retryAfter", ex.getRetryAfterSeconds());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(InsufficientTokensException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientTokens(InsufficientTokensException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Quota exhausted");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Resource not found");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("details", errors);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad request");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal server error");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
