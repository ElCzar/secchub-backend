package co.edu.puj.secchub_backend.admin.exception;

/**
 * Exception thrown when a section is not found.
 */
public class SectionNotFoundException extends RuntimeException {
    public SectionNotFoundException(String message) {
        super(message);
    }
}
