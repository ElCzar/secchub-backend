package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a class is not found in the planning module.
 * This exception is used to indicate that a requested class does not exist.
 */
public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException(String message) {
        super(message);
    }
}