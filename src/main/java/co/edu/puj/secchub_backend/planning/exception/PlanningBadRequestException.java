package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a planning operation is invalid or not allowed.
 */
public class PlanningBadRequestException extends RuntimeException {
    
    public PlanningBadRequestException(String message) {
        super(message);
    }
    
    public PlanningBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}