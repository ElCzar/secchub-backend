package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a student application request is invalid or malformed.
 * This exception indicates that the provided data does not meet the required criteria
 * for creating or processing a student application.·
 */
public class StudentApplicationBadRequestException extends RuntimeException {
    public StudentApplicationBadRequestException(String message) {
        super(message);
    }
}
