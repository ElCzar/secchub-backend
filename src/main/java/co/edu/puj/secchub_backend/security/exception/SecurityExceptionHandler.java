package co.edu.puj.secchub_backend.security.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;


@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.security")
@Slf4j
public class SecurityExceptionHandler {    
    
    @ExceptionHandler(JwtAuthenticationException.class)
    public void handleJwtAuthenticationException(JwtAuthenticationException ex) {
        log.error("JWT authentication failed: {}", ex.getMessage());
    }
}
