package com.library.exception;

/**
 * General business validation exception for library operations
 * (e.g. duplicate ISBN, duplicate member email, inactive member, duplicate return).
 * Handled gracefully by GlobalExceptionHandler with HTTP 400 Bad Request.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }
}
