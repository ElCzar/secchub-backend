package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when there is a bad request related to Teaching Assistant operations.
 */
public class TeachingAssistantBadRequestException extends RuntimeException {
    public TeachingAssistantBadRequestException(String message) {
        super(message);
    }
}
