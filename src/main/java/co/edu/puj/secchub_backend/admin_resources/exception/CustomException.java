package co.edu.puj.secchub_backend.admin_resources.exception;

/**
 * Custom runtime exception for the admin_resources module.
 * 
 * <p>This exception is used throughout the academic planning module to handle
 * business rule violations, validation errors, and operational failures.
 * It provides a consistent way to communicate errors to the client with
 * meaningful messages that can be displayed to users.</p>
 * 
 * <p>Common use cases include:
 * <ul>
 * <li>Business rule violations (e.g., capacity exceeded, duplicate assignments)</li>
 * <li>Data validation failures (e.g., invalid dates, missing required fields)</li>
 * <li>Resource not found scenarios</li>
 * <li>Operational constraints violations (e.g., teacher workload limits)</li>
 * </ul></p>
 * 
 * <p>This exception is handled centrally by the ExceptionHandler to provide
 * consistent error responses across all endpoints.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
public class CustomException extends RuntimeException {
    
    /**
     * Constructs a new CustomException with the specified detail message.
     * 
     * @param message the detail message explaining the reason for the exception
     */
    public CustomException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new CustomException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception (which is saved for later retrieval)
     */
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
