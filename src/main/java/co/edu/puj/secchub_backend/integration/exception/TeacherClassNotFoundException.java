package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a teacher class is not found.
 */
public class TeacherClassNotFoundException extends RuntimeException {
    public TeacherClassNotFoundException(String message) {
        super(message);
    }
}
