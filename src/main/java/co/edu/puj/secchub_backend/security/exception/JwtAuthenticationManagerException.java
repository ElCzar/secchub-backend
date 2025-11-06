package co.edu.puj.secchub_backend.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationManagerException extends AuthenticationException {
    public JwtAuthenticationManagerException(String message) {
        super(message);
    }
}
