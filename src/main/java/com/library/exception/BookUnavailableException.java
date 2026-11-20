package com.library.exception;

/**
 * Exception thrown when attempting to issue a book that has zero available copies.
 * Handled gracefully by GlobalExceptionHandler with HTTP 400 Bad Request.
 */
public class BookUnavailableException extends RuntimeException {

    public BookUnavailableException(String message) {
        super(message);
    }
}
