package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when an academic request is not found.
 */
public class AcademicRequestNotFound extends RuntimeException {
    public AcademicRequestNotFound(String message) { super(message); }
}
