package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception for server errors during academic requests.
 */
public class AcademicRequestServerErrorException extends RuntimeException {
    public AcademicRequestServerErrorException(String message) {
        super(message);
    }
}
