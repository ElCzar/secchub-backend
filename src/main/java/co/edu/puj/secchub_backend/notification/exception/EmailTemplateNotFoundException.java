package co.edu.puj.secchub_backend.notification.exception;

/**
 * Exception thrown when an email template is not found.
 */
public class EmailTemplateNotFoundException extends RuntimeException {
    public EmailTemplateNotFoundException(String message) {
        super(message);
    }
}