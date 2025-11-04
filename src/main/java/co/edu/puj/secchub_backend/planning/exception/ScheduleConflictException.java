package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a schedule conflict occurs in the planning module.
 */
public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}