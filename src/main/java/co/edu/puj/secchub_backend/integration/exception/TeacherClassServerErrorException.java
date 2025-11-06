package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a server error occurs while processing teacher-class assignments
 */
public class TeacherClassServerErrorException extends RuntimeException {
    public TeacherClassServerErrorException(String message) {
        super(message);
    }
}
