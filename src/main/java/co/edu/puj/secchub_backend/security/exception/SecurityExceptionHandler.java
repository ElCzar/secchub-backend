package co.edu.puj.secchub_backend.security.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.security")
public class SecurityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    @ExceptionHandler(JwtAuthenticationException.class)
    public void handleJwtAuthenticationException(JwtAuthenticationException ex) {
        logger.error("JWT authentication failed: {}", ex.getMessage());
    }
}
