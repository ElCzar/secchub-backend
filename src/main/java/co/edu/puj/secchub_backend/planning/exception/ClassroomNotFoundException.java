package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a classroom is not found in the planning module.
 */
public class ClassroomNotFoundException extends RuntimeException {
    
    public ClassroomNotFoundException(String message) {
        super(message);
    }
    
    public ClassroomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}