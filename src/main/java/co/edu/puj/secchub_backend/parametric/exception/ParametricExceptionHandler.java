package co.edu.puj.secchub_backend.parametric.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the parametric module.
 * Handles all parametric-related exceptions and provides appropriate HTTP responses.
 */
@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.parametric")
@Slf4j
public class ParametricExceptionHandler {

    /**
     * Response mapping keys.
     */
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    /**
     * Error messages.
     */
    private static final String NOT_FOUND_ERROR_MESSAGE = "Not Found";

    /**
     * Handles ParametricValueNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * This exception is thrown when a parametric value (status, role, etc.) is not found.
     * @param ex the ParametricValueNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ParametricValueNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleParametricValueNotFoundException(ParametricValueNotFoundException ex) {
        log.warn("Parametric value not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}