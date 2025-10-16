package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a teaching assistant is not found.
 */
public class TeachingAssistantNotFoundException extends RuntimeException {
    public TeachingAssistantNotFoundException(String message) {
        super(message);
    }
}