package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when an academic request is invalid or malformed.
 */
public class AcademicRequestBadRequest extends RuntimeException {
    public AcademicRequestBadRequest(String message) {
        super(message);
    }
}
