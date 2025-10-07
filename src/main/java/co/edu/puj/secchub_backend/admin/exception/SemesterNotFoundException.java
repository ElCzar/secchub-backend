package co.edu.puj.secchub_backend.admin.exception;

/**
 * Exception thrown when a semester is not found.
 */
public class SemesterNotFoundException extends RuntimeException {
    public SemesterNotFoundException(String message) {
        super(message);
    }
}
