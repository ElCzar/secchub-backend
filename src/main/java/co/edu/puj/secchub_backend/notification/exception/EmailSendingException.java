package co.edu.puj.secchub_backend.notification.exception;

/**
 * Exception thrown when email sending operations fail.
 */
public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message) {
        super(message);
    }
}