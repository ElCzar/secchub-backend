package co.edu.puj.secchub_backend.integration.exception;

/**
 * Domain exception for business rule violations in HU01.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
