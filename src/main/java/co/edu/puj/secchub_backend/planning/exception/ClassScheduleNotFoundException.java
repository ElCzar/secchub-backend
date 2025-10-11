package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a class schedule is not found in the planning module.
 * This exception is used to indicate that a requested class schedule does not exist.
 */
public class ClassScheduleNotFoundException extends RuntimeException {
    public ClassScheduleNotFoundException(String message) {
        super(message);
    }
}