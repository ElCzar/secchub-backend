package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when there is a server error related to Teaching Assistant operations.
 */
public class TeachingAssistantServerErrorException extends RuntimeException {
    public TeachingAssistantServerErrorException(String message) {
        super(message);
    }
}
