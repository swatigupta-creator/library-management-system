package com.library.exception;

/**
 * Exception thrown when an entity (Book, Member, or Loan) is not found by ID.
 * Handled gracefully by GlobalExceptionHandler with HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
