package co.edu.puj.secchub_backend.integration.exception;

/**
 * Domain exception for business rule violations.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
