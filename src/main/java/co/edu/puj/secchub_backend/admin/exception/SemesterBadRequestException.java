package co.edu.puj.secchub_backend.admin.exception;

/**
 * Exception thrown when a bad request is made related to semesters.
 */
public class SemesterBadRequestException extends RuntimeException {
    public SemesterBadRequestException(String message) {
        super(message);
    }
}
