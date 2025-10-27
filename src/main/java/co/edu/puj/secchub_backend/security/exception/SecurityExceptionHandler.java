package co.edu.puj.secchub_backend.security.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.security")
@Slf4j
public class SecurityExceptionHandler {    
    /**
     * Response mapping keys.
     */
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    /**
     * Error messages.
     */
    private static final String UNAUTHORIZED_ERROR_MESSAGE = "Unauthorized";
    private static final String NOT_FOUND_ERROR_MESSAGE = "Not Found";

    /**
     * Handles JwtAuthenticationException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the JwtAuthenticationException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public Mono<ResponseEntity<Object>> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        log.warn("JWT authentication exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, UNAUTHORIZED_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles UserNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the UserNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles JwtAuthenticationManagerException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the JwtAuthenticationManagerException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(JwtAuthenticationManagerException.class)
    public Mono<ResponseEntity<Object>> handleJwtAuthenticationManagerException(JwtAuthenticationManagerException ex) {
        log.warn("JWT authentication manager exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, UNAUTHORIZED_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}
