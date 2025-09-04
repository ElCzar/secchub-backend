package co.edu.puj.secchub_backend.security.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@ControllerAdvice(basePackages = "co.edu.puj.secchub_backend.security")
@Slf4j
public class SecurityExceptionHandler {    
    
    /**
     * Handles JwtAuthenticationException and returns a Mono<ResponseEntity<Object>>.
     * @param ex the JwtAuthenticationException
     * @return a Mono<ResponseEntity<Object>> with the error details
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    public Mono<ResponseEntity<Object>> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage(),
                "path", "/auth/login"
        );
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body));
    }
}
