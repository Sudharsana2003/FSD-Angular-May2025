package com.hexa.cozyhavenstay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

// NEW IMPORT: Import Spring Security's AccessDeniedException
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime; // Import LocalDateTime for timestamps
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice // This annotation makes it a global exception handler
public class GlobalExceptionHandler {

    // Helper to extract path consistently
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getDescription(false); // Fallback, e.g., "uri=/api/users"
    }

    // Handles ResourceNotFoundException and returns 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = getRequestPath(request);
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Handles DuplicateEntryException (if you have this custom exception) and returns 409 Conflict
    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntryException(DuplicateEntryException ex, WebRequest request) {
        String path = getRequestPath(request);
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Handles IllegalArgumentException (e.g., bad dates, invalid guest counts from service logic)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String path = getRequestPath(request);
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Handles IllegalStateException (e.g., room already booked, booking already cancelled)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        String path = getRequestPath(request);
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), path); // 409 Conflict
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Handles validation errors caused by @Valid annotation on DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String path = getRequestPath(request);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        String errorMessage = "Validation failed: " + errors.entrySet().stream()
                                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                                    .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, path);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // --- NEW: Handles Spring Security's AccessDeniedException and returns 403 Forbidden ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = getRequestPath(request);
        // You can customize the message, or use ex.getMessage() for more detail
        String message = "Access Denied: You do not have sufficient permissions to perform this action.";
        if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
             message += " Details: " + ex.getMessage(); // Optionally include more specific detail from the exception
        }
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN, message, path);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); // Return 403 Forbidden
    }

    // Handles all other unexpected exceptions
    // This should always be the LAST exception handler in the class
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        // It's good practice to log the full exception stack trace for debugging purposes
        // logger.error("Unhandled exception occurred at path: " + path, ex); // If you have a logger

        String message = "An unexpected internal server error occurred.";
        // In production, you might want to prevent sensitive exception messages from being exposed
        // For development, it's fine to include details.
        if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
            message += " Details: " + ex.getMessage();
        }

        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, path);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}