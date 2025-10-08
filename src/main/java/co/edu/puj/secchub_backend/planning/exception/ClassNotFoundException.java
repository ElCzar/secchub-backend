package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a class is not found in the planning module.
 */
public class ClassNotFoundException extends RuntimeException {
    
    public ClassNotFoundException(String message) {
        super(message);
    }
    
    public ClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}