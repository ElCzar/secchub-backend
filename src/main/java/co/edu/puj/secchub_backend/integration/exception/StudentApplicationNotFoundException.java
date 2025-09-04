package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a student application is not found.
 */
public class StudentApplicationNotFoundException extends RuntimeException {
    public StudentApplicationNotFoundException(String message) {
        super(message);
    }
}
