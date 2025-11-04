package co.edu.puj.secchub_backend.integration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Package exception handler for API controllers within integration package.
 * Maps domain and generic exceptions to HTTP responses with error details.
 */
@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.integration.controller")
@Slf4j
public class IntegrationExceptionHandler {
    /**
     * Response mapping keys.
     */
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    /**
     * Error messages for different exception types.
     */
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String BUSINESS_ERROR_MESSAGE = "Business rule violation";
    private static final String NOT_FOUND_ERROR_MESSAGE = "Not found";
    private static final String VALIDATION_ERROR_MESSAGE = "Validation error";

    /**
     * Manages business exceptions and returns a 400 error with details.
     * @param ex Business exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<Object>> handleBusiness(BusinessException ex) {
        log.warn("Business exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, BUSINESS_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages not found exceptions and returns a 404 error with details.
     * @param ex Not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(AcademicRequestNotFound.class)
    public Mono<ResponseEntity<Object>> handleNotFound(AcademicRequestNotFound ex) {
        log.warn("AcademicRequest not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages not found exceptions and returns a 404 error with details.
     * @param ex Not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(RequestScheduleNotFound.class)
    public Mono<ResponseEntity<Object>> handleNotFound(RequestScheduleNotFound ex) {
        log.warn("RequestSchedule not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages not found exceptions and returns a 404 error with details.
     * @param ex Not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(StudentApplicationNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(StudentApplicationNotFoundException ex) {
        log.warn("StudentApplication not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages not found exceptions and returns a 404 error with details.
     * @param ex Not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(TeacherClassNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(TeacherClassNotFoundException ex) {
        log.warn("TeacherClass not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages time parsing exceptions and returns a 400 error with details.
     * @param ex Time parsing exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(TimeParsingException.class)
    public Mono<ResponseEntity<Object>> handleTimeParsingException(TimeParsingException ex) {
        log.warn("Time parsing exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, VALIDATION_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages generic exceptions and returns a 500 error with details.
     * @param ex Generic exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Object>> handleGeneric(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, GENERIC_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages academic request bad request exceptions and returns a 400 error with details.
     * @param ex Academic request bad request exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(AcademicRequestBadRequest.class)
    public Mono<ResponseEntity<Object>> handleAcademicRequestBadRequest(AcademicRequestBadRequest ex) {
        log.warn("Academic request bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, VALIDATION_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages student application bad request exceptions and returns a 400 error with details.
     * @param ex Student application bad request exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(StudentApplicationBadRequestException.class)
    public Mono<ResponseEntity<Object>> handleStudentApplicationBadRequest(StudentApplicationBadRequestException ex) {
        log.warn("Student application bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, VALIDATION_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages teacher class server error exceptions and returns a 500 error with details.
     * @param ex Teacher class server error exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(TeacherClassServerErrorException.class)
    public Mono<ResponseEntity<Object>> handleTeacherClassServerError(TeacherClassServerErrorException ex) {
        log.error("Teacher class server error exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, GENERIC_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages academic request server error exceptions and returns a 500 error with details.
     * @param ex Academic request server error exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(AcademicRequestServerErrorException.class)
    public Mono<ResponseEntity<Object>> handleAcademicRequestServerError(AcademicRequestServerErrorException ex) {
        log.error("Academic request server error exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, GENERIC_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}