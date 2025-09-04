package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a requested schedule is not found.
 */
public class RequestScheduleNotFound extends RuntimeException {
    public RequestScheduleNotFound(String message) {
        super(message);
    }
}
