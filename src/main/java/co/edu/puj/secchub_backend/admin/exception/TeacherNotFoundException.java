package co.edu.puj.secchub_backend.admin.exception;

/**
 * Exception thrown when a teacher is not found.
 */
public class TeacherNotFoundException extends RuntimeException {
    
    public TeacherNotFoundException(String message) {
        super(message);
    }
}