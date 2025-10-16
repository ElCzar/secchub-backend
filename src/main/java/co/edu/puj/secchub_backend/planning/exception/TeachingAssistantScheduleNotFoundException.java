package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a teaching assistant schedule is not found.
 */
public class TeachingAssistantScheduleNotFoundException extends RuntimeException {
    public TeachingAssistantScheduleNotFoundException(String message) {
        super(message);
    }
}