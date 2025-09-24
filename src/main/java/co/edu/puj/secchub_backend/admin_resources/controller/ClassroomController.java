package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassroomDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.service.ClassroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for classroom management.
 * 
 * <p>This controller provides comprehensive endpoints for classroom
 * administration including CRUD operations, search capabilities, and
 * availability checking. It serves as the API layer for classroom
 * management in the academic planning module.</p>
 * 
 * <p>Available endpoints include:
 * <ul>
 * <li>Classroom CRUD operations</li>
 * <li>Search by name, campus, and type</li>
 * <li>Availability checking for scheduling</li>
 * <li>Filtering and pagination support</li>
 * </ul></p>
 * 
 * <p>All endpoints follow RESTful conventions and return appropriate HTTP
 * status codes. Error handling is centralized through the CustomException
 * system with meaningful error messages.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@Slf4j
public class ClassroomController {

    private final ClassroomService classroomService;

    // ==========================================
    // OPERACIONES CRUD
    // ==========================================

    /**
     * Get all classrooms.
     * 
     * @return ResponseEntity containing list of classrooms and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> getAllClassrooms() {
        try {
            log.info("Obteniendo todas las aulas");
            List<ClassroomDTO> classrooms = classroomService.getAllClassrooms();
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error al obtener todas las aulas", e);
            throw new CustomException("Error al obtener las aulas");
        }
    }

    /**
     * Get classroom by ID.
     * 
     * @param id the classroom ID
     * @return ResponseEntity containing classroom DTO and HTTP 200 status
     * @throws CustomException if classroom not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDTO> getClassroomById(@PathVariable Long id) {
        try {
            log.info("Obteniendo aula por ID: {}", id);
            ClassroomDTO classroom = classroomService.getClassroomById(id);
            return ResponseEntity.ok(classroom);
        } catch (CustomException e) {
            log.error("Error al obtener aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener aula {}", id, e);
            throw new CustomException("Error interno del servidor al obtener el aula");
        }
    }

    /**
     * Create a new classroom.
     * 
     * @param classroomDTO the classroom data
     * @return ResponseEntity containing created classroom DTO and HTTP 201 status
     * @throws CustomException if validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ClassroomDTO> createClassroom(@RequestBody ClassroomDTO classroomDTO) {
        try {
            log.info("Creando nueva aula: {}", classroomDTO.getRoom());
            ClassroomDTO createdClassroom = classroomService.createClassroom(classroomDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClassroom);
        } catch (CustomException e) {
            log.error("Error al crear aula: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear aula", e);
            throw new CustomException("Error interno del servidor al crear el aula");
        }
    }

    /**
     * Update an existing classroom.
     * 
     * @param id the classroom ID
     * @param classroomDTO the updated classroom data
     * @return ResponseEntity containing updated classroom DTO and HTTP 200 status
     * @throws CustomException if classroom not found or validation fails
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ClassroomDTO> updateClassroom(
            @PathVariable Long id,
            @RequestBody ClassroomDTO classroomDTO) {
        try {
            log.info("Actualizando aula con ID: {}", id);
            ClassroomDTO updatedClassroom = classroomService.updateClassroom(id, classroomDTO);
            return ResponseEntity.ok(updatedClassroom);
        } catch (CustomException e) {
            log.error("Error al actualizar aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar aula {}", id, e);
            throw new CustomException("Error interno del servidor al actualizar el aula");
        }
    }

    /**
     * Delete a classroom.
     * 
     * @param id the classroom ID
     * @return ResponseEntity with HTTP 204 status
     * @throws CustomException if classroom not found or has dependencies
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        try {
            log.info("Eliminando aula con ID: {}", id);
            classroomService.deleteClassroom(id);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            log.error("Error al eliminar aula {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar aula {}", id, e);
            throw new CustomException("Error interno del servidor al eliminar el aula");
        }
    }

    // ==========================================
    // BÚSQUEDA Y FILTRADO
    // ==========================================

    /**
     * Search classrooms by name.
     * 
     * @param name the room name to search for
     * @return ResponseEntity containing list of matching classrooms
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClassroomDTO>> searchClassroomsByName(@RequestParam String name) {
        try {
            log.info("Buscando aulas por nombre: {}", name);
            List<ClassroomDTO> classrooms = classroomService.searchClassroomsByName(name);
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error al buscar aulas por nombre", e);
            throw new CustomException("Error al buscar aulas");
        }
    }

    /**
     * Get classrooms by campus.
     * 
     * @param campus the campus name
     * @return ResponseEntity containing list of classrooms in the campus
     */
    @GetMapping("/campus/{campus}")
    public ResponseEntity<List<ClassroomDTO>> getClassroomsByCampus(@PathVariable String campus) {
        try {
            log.info("Obteniendo aulas del campus: {}", campus);
            List<ClassroomDTO> classrooms = classroomService.getClassroomsByCampus(campus);
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error al obtener aulas del campus {}", campus, e);
            throw new CustomException("Error al obtener aulas del campus");
        }
    }

    /**
     * Get classrooms by type.
     * 
     * @param typeId the classroom type ID
     * @return ResponseEntity containing list of classrooms of the specified type
     */
    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<ClassroomDTO>> getClassroomsByType(@PathVariable Long typeId) {
        try {
            log.info("Obteniendo aulas del tipo: {}", typeId);
            List<ClassroomDTO> classrooms = classroomService.getClassroomsByType(typeId);
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error al obtener aulas del tipo {}", typeId, e);
            throw new CustomException("Error al obtener aulas del tipo especificado");
        }
    }

    /**
     * Get available classrooms for a specific time slot.
     * 
     * @param day the day of the week
     * @param startTime the start time
     * @param endTime the end time
     * @param minCapacity optional minimum capacity
     * @return ResponseEntity containing list of available classrooms
     */
    @GetMapping("/available")
    public ResponseEntity<List<ClassroomDTO>> getAvailableClassrooms(
            @RequestParam String day,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Integer minCapacity) {
        try {
            log.info("Buscando aulas disponibles para {} de {} a {}", day, startTime, endTime);
            List<ClassroomDTO> classrooms = classroomService.getAvailableClassrooms(day, startTime, endTime, minCapacity);
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error al buscar aulas disponibles", e);
            throw new CustomException("Error al buscar aulas disponibles");
        }
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("Error de negocio en aulas: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error de validación",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido en aulas: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Parámetro inválido",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado en aulas", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno del servidor",
                    "message", "Ha ocurrido un error inesperado en la gestión de aulas",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }
}
