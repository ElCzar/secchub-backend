package co.edu.puj.secchub_backend.admin_resources.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the admin_resources module.
 * 
 * <p>This class provides centralized exception handling for all controllers
 * in the admin_resources package. It ensures consistent error responses
 * across all endpoints and provides meaningful error messages to clients.</p>
 * 
 * <p>Handled exception types include:
 * <ul>
 * <li>CustomException - Business logic and validation errors</li>
 * <li>MethodArgumentNotValidException - Bean validation errors</li>
 * <li>IllegalArgumentException - Invalid method arguments</li>
 * <li>RuntimeException - Generic runtime errors</li>
 * </ul></p>
 * 
 * <p>All error responses include a timestamp, error message, and appropriate
 * HTTP status code. Validation errors include field-specific error details.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.admin_resources")
@Slf4j
public class ExceptionHandler {

    /**
     * Handles CustomException thrown by business logic operations.
     * 
     * @param ex the CustomException that was thrown
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Business Logic Error");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors from Bean Validation annotations.
     * 
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with field-specific error details and HTTP 400 status
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Error");
        errorResponse.put("message", "Request validation failed");
        errorResponse.put("fieldErrors", fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalArgumentException for invalid method parameters.
     * 
     * @param ex the IllegalArgumentException that was thrown
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Invalid Argument");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic RuntimeException as a fallback.
     * 
     * @param ex the RuntimeException that was thrown
     * @return ResponseEntity with error details and HTTP 500 status
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred. Please try again later.");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
