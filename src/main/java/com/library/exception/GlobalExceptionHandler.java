package com.library.exception;

import com.library.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Exception Handling demonstrating:
 * 1. Exception Handling with proper HTTP status codes and clear error messages.
 * 2. Generics & Collections (ApiResponse<Map<String, String>>, HashMap)
 * 3. Elimination of empty catch blocks or hidden exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles form / request validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String firstMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        ApiResponse<Map<String, String>> response = new ApiResponse<>(false, firstMessage, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handles missing resource (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    // Handles unauthorized librarian access (401 Unauthorized)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    // Handles book stock unavailable (400 Bad Request)
    @ExceptionHandler(BookUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookUnavailable(BookUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    // Handles general library business logic constraints (400 Bad Request)
    @ExceptionHandler(LibraryException.class)
    public ResponseEntity<ApiResponse<Void>> handleLibraryException(LibraryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    // Fallback handler for uncaught system errors (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal Server Error: " + ex.getMessage()));
    }
}
