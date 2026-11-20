package com.library.constant;

/**
 * Global application constants demonstrating:
 * - Access Control (public class, private constructor)
 * - Static and Final keywords (immutable constants shared across application)
 */
public final class LibraryConstants {

    // Prevent instantiation of utility/constants class
    private LibraryConstants() {
        throw new UnsupportedOperationException("LibraryConstants is a static utility class and cannot be instantiated");
    }

    // Default book borrowing period in days
    public static final int DEFAULT_LOAN_DAYS = 14;

    // Maximum books allowed to be borrowed simultaneously by one member
    public static final int MAX_ACTIVE_LOANS_PER_MEMBER = 5;

    // Member status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // Loan status constants
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_RETURNED = "RETURNED";
}
