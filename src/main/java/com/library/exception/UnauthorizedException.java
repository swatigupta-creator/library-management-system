package com.library.exception;

/**
 * Exception thrown when a protected librarian operation is requested
 * without an authenticated session. Handled with HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
