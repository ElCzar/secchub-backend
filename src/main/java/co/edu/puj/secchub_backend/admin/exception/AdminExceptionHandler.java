package co.edu.puj.secchub_backend.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;


@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.admin")
@Slf4j
public class AdminExceptionHandler {
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
    private static final String BAD_REQUEST_ERROR_MESSAGE = "Bad Request";

    /**
     * Manages course not found exceptions and returns a 404 error with details.
     * @param ex Course not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(CourseNotFoundException ex) {
        log.warn("Course not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages section not found exceptions and returns a 404 error with details.
     * @param ex Section not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(SectionNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(SectionNotFoundException ex) {
        log.warn("Section not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages semester bad request exceptions and returns a 400 error with details.
     * @param ex Semester bad request exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(SemesterBadRequestException.class)
    public Mono<ResponseEntity<Object>> handleBadRequest(SemesterBadRequestException ex) {
        log.warn("Semester bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, BAD_REQUEST_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages semester not found exceptions and returns a 404 error with details.
     * @param ex Semester not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(SemesterNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(SemesterNotFoundException ex) {
        log.warn("Semester not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Manages teacher not found exceptions and returns a 404 error with details.
     * @param ex Teacher not found exception
     * @return HTTP response with error information
     */
    @ExceptionHandler(TeacherNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleNotFound(TeacherNotFoundException ex) {
        log.warn("Teacher not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, Instant.now().toString(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}
