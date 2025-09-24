package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.dto.ModalityDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.service.ModalityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for modality management.
 * 
 * <p>This controller provides endpoints for modality administration
 * including CRUD operations and retrieval. It serves as the API layer
 * for modality management in the academic planning module.</p>
 * 
 * <p>Available endpoints include:
 * <ul>
 * <li>Modality CRUD operations</li>
 * <li>Ordered retrieval for UI display</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/modalities")
@RequiredArgsConstructor
@Slf4j
public class ModalityController {

    private final ModalityService modalityService;

    // ==========================================
    // OPERACIONES CRUD
    // ==========================================

    /**
     * Get all modalities.
     * 
     * @return ResponseEntity containing list of modalities and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<ModalityDTO>> getAllModalities() {
        try {
            log.info("Obteniendo todas las modalidades");
            List<ModalityDTO> modalities = modalityService.getAllModalities();
            return ResponseEntity.ok(modalities);
        } catch (Exception e) {
            log.error("Error al obtener todas las modalidades", e);
            throw new CustomException("Error al obtener las modalidades");
        }
    }

    /**
     * Get modality by ID.
     * 
     * @param id the modality ID
     * @return ResponseEntity containing modality DTO and HTTP 200 status
     * @throws CustomException if modality not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModalityDTO> getModalityById(@PathVariable Long id) {
        try {
            log.info("Obteniendo modalidad por ID: {}", id);
            ModalityDTO modality = modalityService.getModalityById(id);
            return ResponseEntity.ok(modality);
        } catch (CustomException e) {
            log.error("Error al obtener modalidad {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener modalidad {}", id, e);
            throw new CustomException("Error interno del servidor al obtener la modalidad");
        }
    }

    /**
     * Create a new modality.
     * 
     * @param modalityDTO the modality data
     * @return ResponseEntity containing created modality DTO and HTTP 201 status
     * @throws CustomException if validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ModalityDTO> createModality(@RequestBody ModalityDTO modalityDTO) {
        try {
            log.info("Creando nueva modalidad: {}", modalityDTO.getName());
            ModalityDTO createdModality = modalityService.createModality(modalityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdModality);
        } catch (CustomException e) {
            log.error("Error al crear modalidad: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear modalidad", e);
            throw new CustomException("Error interno del servidor al crear la modalidad");
        }
    }

    /**
     * Update an existing modality.
     * 
     * @param id the modality ID
     * @param modalityDTO the updated modality data
     * @return ResponseEntity containing updated modality DTO and HTTP 200 status
     * @throws CustomException if modality not found or validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ModalityDTO> updateModality(
            @PathVariable Long id,
            @RequestBody ModalityDTO modalityDTO) {
        try {
            log.info("Actualizando modalidad con ID: {}", id);
            ModalityDTO updatedModality = modalityService.updateModality(id, modalityDTO);
            return ResponseEntity.ok(updatedModality);
        } catch (CustomException e) {
            log.error("Error al actualizar modalidad {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar modalidad {}", id, e);
            throw new CustomException("Error interno del servidor al actualizar la modalidad");
        }
    }

    /**
     * Delete a modality.
     * 
     * @param id the modality ID
     * @return ResponseEntity with HTTP 204 status
     * @throws CustomException if modality not found or has dependencies
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModality(@PathVariable Long id) {
        try {
            log.info("Eliminando modalidad con ID: {}", id);
            modalityService.deleteModality(id);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            log.error("Error al eliminar modalidad {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar modalidad {}", id, e);
            throw new CustomException("Error interno del servidor al eliminar la modalidad");
        }
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("Error de negocio en modalidades: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error de validación",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido en modalidades: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Parámetro inválido",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado en modalidades", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno del servidor",
                    "message", "Ha ocurrido un error inesperado en la gestión de modalidades",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }
}
