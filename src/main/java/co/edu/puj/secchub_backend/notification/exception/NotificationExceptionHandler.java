package co.edu.puj.secchub_backend.notification.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.notification")
@Slf4j
public class NotificationExceptionHandler {
    /**
     * Response mapping keys.
     */
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    /**
     * Error messages.
     */
    private static final String EMAIL_SENDING_ERROR_MESSAGE = "Email Sending Failed";
    private static final String EMAIL_TEMPLATE_NOT_FOUND_ERROR_MESSAGE = "Email Template Not Found";

    /**
     * Handles EmailSendingException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the EmailSendingException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(EmailSendingException.class)
    public Mono<ResponseEntity<Object>> handleEmailSendingException(EmailSendingException ex) {
        log.warn("Email sending exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, EMAIL_SENDING_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles EmailTemplateNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the EmailTemplateNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(EmailTemplateNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleEmailTemplateNotFoundException(EmailTemplateNotFoundException ex) {
        log.warn("Email template not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, EMAIL_TEMPLATE_NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}