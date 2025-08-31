package co.edu.puj.secchub_backend.integration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * Mapea excepciones de dominio y genéricas a respuestas HTTP con detalles del error.
 */
@ControllerAdvice
public class ApiExceptionHandler {

    /**
     * Maneja excepciones de negocio y retorna un error 400 con detalles.
     * @param ex Excepción de negocio
     * @return Respuesta HTTP con información del error
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Business rule violation",
                "message", ex.getMessage()
        ));
    }

    /**
     * Maneja excepciones genéricas y retorna un error 500 con detalles.
     * @param ex Excepción genérica
     * @return Respuesta HTTP con información del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Internal error",
                "message", ex.getMessage()
        ));
    }
}

