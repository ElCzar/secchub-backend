package co.edu.puj.secchub_backend.planning.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the planning module.
 * Handles all planning-related exceptions and provides appropriate HTTP responses.
 */
@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.planning")
@Slf4j
public class PlanningExceptionHandler {

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
    private static final String CONFLICT_ERROR_MESSAGE = "Conflict";

    /**
     * Handles ClassNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ClassNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ClassNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleClassNotFoundException(ClassNotFoundException ex) {
        log.warn("Class not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles ClassroomNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ClassroomNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ClassroomNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleClassroomNotFoundException(ClassroomNotFoundException ex) {
        log.warn("Classroom not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles TeachingAssistantNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the TeachingAssistantNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(TeachingAssistantNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleTeachingAssistantNotFoundException(TeachingAssistantNotFoundException ex) {
        log.warn("Teaching assistant not found exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, NOT_FOUND_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles ScheduleConflictException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ScheduleConflictException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ScheduleConflictException.class)
    public Mono<ResponseEntity<Object>> handleScheduleConflictException(ScheduleConflictException ex) {
        log.warn("Schedule conflict exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, CONFLICT_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles PlanningBadRequestException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the PlanningBadRequestException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(PlanningBadRequestException.class)
    public Mono<ResponseEntity<Object>> handlePlanningBadRequestException(PlanningBadRequestException ex) {
        log.warn("Planning bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, BAD_REQUEST_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}