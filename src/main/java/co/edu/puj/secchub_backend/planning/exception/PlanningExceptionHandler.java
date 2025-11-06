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
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

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
     * Handles ClassScheduleNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ClassScheduleNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ClassScheduleNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleClassScheduleNotFoundException(ClassScheduleNotFoundException ex) {
        log.warn("Class schedule not found exception occurred: {}", ex.getMessage());
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
     * Handles TeachingAssistantScheduleNotFoundException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the TeachingAssistantScheduleNotFoundException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(TeachingAssistantScheduleNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleTeachingAssistantScheduleNotFoundException(TeachingAssistantScheduleNotFoundException ex) {
        log.warn("Teaching assistant schedule not found exception occurred: {}", ex.getMessage());
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

    /**
     * Handles ClassroomBadRequestException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ClassroomBadRequestException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ClassroomBadRequestException.class)
    public Mono<ResponseEntity<Object>> handleClassroomBadRequestException(ClassroomBadRequestException ex) {
        log.warn("Classroom bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, BAD_REQUEST_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles ClassCreationException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the ClassCreationException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(ClassCreationException.class)
    public Mono<ResponseEntity<Object>> handleClassCreationException(ClassCreationException ex) {
        log.error("Class creation exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, INTERNAL_SERVER_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles PlanningServerErrorException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the PlanningServerErrorException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(PlanningServerErrorException.class)
    public Mono<ResponseEntity<Object>> handlePlanningServerErrorException(PlanningServerErrorException ex) {
        log.error("Planning server error exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, INTERNAL_SERVER_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles TeachingAssistantBadRequestException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the TeachingAssistantBadRequestException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(TeachingAssistantBadRequestException.class)
    public Mono<ResponseEntity<Object>> handleTeachingAssistantBadRequestException(TeachingAssistantBadRequestException ex) {
        log.warn("Teaching assistant bad request exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, BAD_REQUEST_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }

    /**
     * Handles TeachingAssistantServerErrorException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the TeachingAssistantServerErrorException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(TeachingAssistantServerErrorException.class)
    public Mono<ResponseEntity<Object>> handleTeachingAssistantServerErrorException(TeachingAssistantServerErrorException ex) {
        log.error("Teaching assistant server error exception occurred: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP_KEY, System.currentTimeMillis(),
                ERROR_KEY, INTERNAL_SERVER_ERROR_MESSAGE,
                MESSAGE_KEY, ex.getMessage()
        )));
    }
}