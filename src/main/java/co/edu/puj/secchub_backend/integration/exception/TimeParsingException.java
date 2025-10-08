package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a time string cannot be parsed to a valid Time object.
 * This exception indicates that the provided time format is invalid or malformed.
 */
public class TimeParsingException extends RuntimeException {
    
    /**
     * Creates a new TimeParsingException with the specified message.
     * @param message the detail message explaining the parsing error
     */
    public TimeParsingException(String message) {
        super(message);
    }
    
    /**
     * Creates a new TimeParsingException with the specified message and cause.
     * @param message the detail message explaining the parsing error
     * @param cause the cause of this exception
     */
    public TimeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}