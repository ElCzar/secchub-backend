package co.edu.puj.secchub_backend.planning.exception;

/**
 * Exception thrown when a bad request is made related to classroom operations.
 */
public class ClassroomBadRequestException extends RuntimeException {
    public ClassroomBadRequestException(String message) {
        super(message);
    }
}
