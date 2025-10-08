package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassroomTypeDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.service.ClassroomTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for classroom type management.
 * 
 * <p>This controller provides endpoints for classroom type administration
 * including CRUD operations and retrieval. It serves as the API layer
 * for classroom type management in the academic planning module.</p>
 * 
 * <p>Available endpoints include:
 * <ul>
 * <li>Classroom type CRUD operations</li>
 * <li>Ordered retrieval for UI display</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/classroom-types")
@RequiredArgsConstructor
@Slf4j
public class ClassroomTypeController {

    private final ClassroomTypeService classroomTypeService;

    // ==========================================
    // OPERACIONES CRUD
    // ==========================================

    /**
     * Get all classroom types.
     * 
     * @return ResponseEntity containing list of classroom types and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<ClassroomTypeDTO>> getAllClassroomTypes() {
        try {
            log.info("Obteniendo todos los tipos de aula");
            List<ClassroomTypeDTO> classroomTypes = classroomTypeService.getAllClassroomTypes();
            return ResponseEntity.ok(classroomTypes);
        } catch (Exception e) {
            log.error("Error al obtener todos los tipos de aula", e);
            throw new CustomException("Error al obtener los tipos de aula");
        }
    }

    /**
     * Get classroom type by ID.
     * 
     * @param id the classroom type ID
     * @return ResponseEntity containing classroom type DTO and HTTP 200 status
     * @throws CustomException if classroom type not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomTypeDTO> getClassroomTypeById(@PathVariable Long id) {
        try {
            log.info("Obteniendo tipo de aula por ID: {}", id);
            ClassroomTypeDTO classroomType = classroomTypeService.getClassroomTypeById(id);
            return ResponseEntity.ok(classroomType);
        } catch (CustomException e) {
            log.error("Error al obtener tipo de aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener tipo de aula {}", id, e);
            throw new CustomException("Error interno del servidor al obtener el tipo de aula");
        }
    }

    /**
     * Create a new classroom type.
     * 
     * @param classroomTypeDTO the classroom type data
     * @return ResponseEntity containing created classroom type DTO and HTTP 201 status
     * @throws CustomException if validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ClassroomTypeDTO> createClassroomType(@RequestBody ClassroomTypeDTO classroomTypeDTO) {
        try {
            log.info("Creando nuevo tipo de aula: {}", classroomTypeDTO.getName());
            ClassroomTypeDTO createdClassroomType = classroomTypeService.createClassroomType(classroomTypeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClassroomType);
        } catch (CustomException e) {
            log.error("Error al crear tipo de aula: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear tipo de aula", e);
            throw new CustomException("Error interno del servidor al crear el tipo de aula");
        }
    }

    /**
     * Update an existing classroom type.
     * 
     * @param id the classroom type ID
     * @param classroomTypeDTO the updated classroom type data
     * @return ResponseEntity containing updated classroom type DTO and HTTP 200 status
     * @throws CustomException if classroom type not found or validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClassroomTypeDTO> updateClassroomType(
            @PathVariable Long id,
            @RequestBody ClassroomTypeDTO classroomTypeDTO) {
        try {
            log.info("Actualizando tipo de aula con ID: {}", id);
            ClassroomTypeDTO updatedClassroomType = classroomTypeService.updateClassroomType(id, classroomTypeDTO);
            return ResponseEntity.ok(updatedClassroomType);
        } catch (CustomException e) {
            log.error("Error al actualizar tipo de aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar tipo de aula {}", id, e);
            throw new CustomException("Error interno del servidor al actualizar el tipo de aula");
        }
    }

    /**
     * Delete a classroom type.
     * 
     * @param id the classroom type ID
     * @return ResponseEntity with HTTP 204 status
     * @throws CustomException if classroom type not found or has dependencies
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroomType(@PathVariable Long id) {
        try {
            log.info("Eliminando tipo de aula con ID: {}", id);
            classroomTypeService.deleteClassroomType(id);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            log.error("Error al eliminar tipo de aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar tipo de aula {}", id, e);
            throw new CustomException("Error interno del servidor al eliminar el tipo de aula");
        }
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("Error de negocio en tipos de aula: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error de validación",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido en tipos de aula: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Parámetro inválido",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado en tipos de aula", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno del servidor",
                    "message", "Ha ocurrido un error inesperado en la gestión de tipos de aula",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }
}
