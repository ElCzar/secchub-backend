package co.edu.puj.secchub_backend.integration.exception;

/**
 * Exception thrown when a course is not found.
 */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String message) {
        super(message);
    }
}
