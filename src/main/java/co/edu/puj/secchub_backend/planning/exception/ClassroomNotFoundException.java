package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a classroom is not found in the planning module.
 * This exception is used to indicate that a requested classroom does not exist.
 */
public class ClassroomNotFoundException extends RuntimeException {
    public ClassroomNotFoundException(String message) {
        super(message);
    }
}