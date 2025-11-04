package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when there is an error during class creation.
 */
public class ClassCreationException extends RuntimeException {
    public ClassCreationException(String message) {
        super(message);
    }
}
