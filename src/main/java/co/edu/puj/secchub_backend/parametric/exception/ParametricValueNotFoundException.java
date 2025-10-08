package co.edu.puj.secchub_backend.parametric.exception;

/**
 * Exception thrown when a parametric value (status, role, etc.) is not found.
 */
public class ParametricValueNotFoundException extends RuntimeException {
    
    public ParametricValueNotFoundException(String message) {
        super(message);
    }
    
    public ParametricValueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}