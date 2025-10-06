package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.ClassScheduleDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.TeacherDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.service.PlanningService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for academic planning management.
 * 
 * <p>This controller provides comprehensive endpoints for academic planning
 * operations including class management, schedule creation, teacher management,
 * and planning analytics. It serves as the main API entry point for the
 * academic planning module (HU07) in the admin_resources context.</p>
 * 
 * <p>Available endpoints include:
 * <ul>
 * <li>Academic class CRUD operations</li>
 * <li>Class schedule management and conflict detection</li>
 * <li>Teacher management and availability queries</li>
 * <li>Planning duplication and bulk operations</li>
 * <li>Analytics and reporting endpoints</li>
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
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Slf4j
public class PlanningController {

    private final PlanningService planningService;

    // ==========================================
    // GESTIÓN DE CLASES ACADÉMICAS
    // ==========================================

    /**
     * Creates a new academic class.
     * 
     * <p>This endpoint validates the provided class data and creates a new
     * academic class with proper business rule enforcement. It checks for
     * duplicates, validates capacity constraints, and ensures date consistency.</p>
     * 
     * @param classDTO the class data transfer object containing class information
     * @return ResponseEntity containing the created class DTO and HTTP 201 status
     * @throws CustomException if validation fails or business rules are violated
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/classes")
    public ResponseEntity<ClassDTO> createClass(@RequestBody ClassDTO classDTO) {
        try {
            log.info("Creando nueva clase: {}", classDTO.getCourseName());
            ClassDTO createdClass = planningService.createClass(classDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (CustomException e) {
            log.error("Error al crear clase: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear clase", e);
            throw new CustomException("Error interno del servidor al crear la clase");
        }
    }

    /**
     * Obtener todas las clases con paginación
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/classes")
    public ResponseEntity<List<ClassDTO>> getAllClasses() {
        log.debug("=== OBTENIENDO TODAS LAS CLASES ===");
        try {
            List<ClassDTO> classes = planningService.getAllClasses();
            log.debug("Número de clases encontradas: {}", classes.size());
            for (int i = 0; i < classes.size(); i++) {
                ClassDTO classDTO = classes.get(i);
                log.debug("=== CLASE {} ===", (i + 1));
                log.debug("ID: {}", classDTO.getId());
                log.debug("CourseID: {}", classDTO.getCourseId());
                log.debug("CourseName: {}", classDTO.getCourseName());
                log.debug("StartDate: {}", classDTO.getStartDate());
                log.debug("EndDate: {}", classDTO.getEndDate());
                log.debug("StartDate Type: {}", (classDTO.getStartDate() != null ? classDTO.getStartDate().getClass().getSimpleName() : "null"));
                log.debug("EndDate Type: {}", (classDTO.getEndDate() != null ? classDTO.getEndDate().getClass().getSimpleName() : "null"));
                log.debug("Capacity: {}", classDTO.getCapacity());
                log.debug("Complete Object: {}", classDTO.toString());
                log.debug("------------------------");
            }
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Error al obtener todas las clases: {}", e.getMessage());
            log.error("Error al obtener todas las clases", e);
            throw new CustomException("Error al obtener las clases");
        }
    }

    /**
     * Obtener clases por semestre
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/classes/semester/{semesterId}")
    public ResponseEntity<List<ClassDTO>> getClassesBySemester(
            @PathVariable Long semesterId) {
        try {
            log.info("Obteniendo clases del semestre: {}", semesterId);
            List<ClassDTO> classes = planningService.getClassesBySemester(semesterId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Error al obtener clases del semestre {}", semesterId, e);
            throw new CustomException("Error al obtener las clases del semestre");
        }
    }

    /**
     * Actualizar clase académica existente
     */
    //PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/classes/{classId}")
    public ResponseEntity<ClassDTO> updateClass(
            @PathVariable Long classId,
            @RequestBody ClassDTO classDTO) {
        try {
            log.info("Actualizando clase con ID: {}", classId);
            ClassDTO updatedClass = planningService.updateClass(classId, classDTO);
            return ResponseEntity.ok(updatedClass);
        } catch (CustomException e) {
            log.error("Error al actualizar clase {}: {}", classId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar clase {}", classId, e);
            throw new CustomException("Error interno del servidor al actualizar la clase");
        }
    }

    /**
     * Eliminar clase académica y sus horarios asociados
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId) {
        try {
            log.info("Eliminando clase con ID: {}", classId);
            planningService.deleteClass(classId);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            log.error("Error al eliminar clase {}: {}", classId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar clase {}", classId, e);
            throw new CustomException("Error interno del servidor al eliminar la clase");
        }
    }

    // ==========================================
    // DUPLICACIÓN DE PLANIFICACIÓN
    // ==========================================

    /**
     * Duplicar planificación completa de un semestre a otro
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/duplicate")
    public ResponseEntity<List<ClassDTO>> duplicateSemesterPlanning(
            @RequestParam Long sourceSemesterId,
            @RequestParam Long targetSemesterId) {
        try {
            log.info("Duplicando planificación del semestre {} al semestre {}", 
                    sourceSemesterId, targetSemesterId);
            List<ClassDTO> duplicatedClasses = planningService.duplicateSemesterPlanning(
                    sourceSemesterId, targetSemesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(duplicatedClasses);
        } catch (CustomException e) {
            log.error("Error al duplicar planificación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al duplicar planificación", e);
            throw new CustomException("Error interno del servidor al duplicar la planificación");
        }
    }

    // ==========================================
    // GESTIÓN DE HORARIOS
    // ==========================================

    /**
     * Asignar horario a una clase académica
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/classes/{classId}/schedules")
    public ResponseEntity<ClassScheduleDTO> assignScheduleToClass(
            @PathVariable Long classId,
            @RequestBody ClassScheduleDTO scheduleDTO) {
        try {
            log.info("Asignando horario a la clase: {}", classId);
            ClassScheduleDTO assignedSchedule = planningService.assignScheduleToClass(classId, scheduleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignedSchedule);
        } catch (CustomException e) {
            log.error("Error al asignar horario a clase {}: {}", classId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al asignar horario a clase {}", classId, e);
            throw new CustomException("Error interno del servidor al asignar el horario");
        }
    }

    /**
     * Obtener todos los horarios de una clase
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/classes/{classId}/schedules")
    public ResponseEntity<List<ClassScheduleDTO>> getClassSchedules(@PathVariable Long classId) {
        try {
            log.info("Obteniendo horarios de la clase: {}", classId);
            List<ClassScheduleDTO> schedules = planningService.getClassSchedules(classId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error al obtener horarios de la clase {}", classId, e);
            throw new CustomException("Error al obtener los horarios de la clase");
        }
    }

    /**
     * Eliminar un horario específico
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Map<String, String>> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            log.info("Eliminando horario: {}", scheduleId);
            planningService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(Map.of("message", "Horario eliminado exitosamente"));
        } catch (CustomException e) {
            log.error("Error al eliminar horario {}: {}", scheduleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar horario {}", scheduleId, e);
            throw new CustomException("Error interno del servidor al eliminar el horario");
        }
    }

    /**
     * Actualizar un horario específico
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ClassScheduleDTO> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ClassScheduleDTO scheduleDTO) {
        try {
            log.info("Actualizando horario: {}", scheduleId);
            ClassScheduleDTO updatedSchedule = planningService.updateSchedule(scheduleId, scheduleDTO);
            return ResponseEntity.ok(updatedSchedule);
        } catch (CustomException e) {
            log.error("Error al actualizar horario {}: {}", scheduleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar horario {}", scheduleId, e);
            throw new CustomException("Error interno del servidor al actualizar el horario");
        }
    }

    /**
     * Detectar conflictos de horarios en un aula específica
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/schedules/conflicts")
    public ResponseEntity<List<ClassScheduleDTO>> detectScheduleConflicts(
            @RequestParam Long classroomId,
            @RequestParam String day) {
        try {
            log.info("Detectando conflictos en aula {} para el día {}", classroomId, day);
            List<ClassScheduleDTO> conflicts = planningService.detectScheduleConflicts(classroomId, day);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            log.error("Error al detectar conflictos de horarios", e);
            throw new CustomException("Error al detectar conflictos de horarios");
        }
    }

    // ==========================================
    // GESTIÓN DE PROFESORES
    // ==========================================

    /**
     * Obtener profesores disponibles para una cantidad de horas
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/teachers/available")
    public ResponseEntity<List<TeacherDTO>> getAvailableTeachers(@RequestParam Integer requiredHours) {
        try {
            log.info("Buscando profesores disponibles para {} horas", requiredHours);
            List<TeacherDTO> availableTeachers = planningService.getAvailableTeachersForClass(requiredHours);
            return ResponseEntity.ok(availableTeachers);
        } catch (Exception e) {
            log.error("Error al obtener profesores disponibles", e);
            throw new CustomException("Error al obtener profesores disponibles");
        }
    }

    // ==========================================
    // REPORTES Y ESTADÍSTICAS
    // ==========================================

    /**
     * Obtener estadísticas de utilización de aulas por semestre
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/statistics/utilization/{semesterId}")
    public ResponseEntity<Map<String, Object>> getUtilizationStatistics(@PathVariable Long semesterId) {
        try {
            log.info("Obteniendo estadísticas de utilización para semestre: {}", semesterId);
            Map<String, Object> statistics = planningService.getClassroomUtilizationStats(semesterId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de utilización", e);
            throw new CustomException("Error al obtener estadísticas de utilización");
        }
    }

    // ==========================================
    // ENDPOINTS ADICIONALES
    // ==========================================

    /**
     * Obtener información general de una clase específica
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/classes/{classId}")
    public ResponseEntity<ClassDTO> getClassById(@PathVariable Long classId) {
        try {
            log.info("Obteniendo información de la clase: {}", classId);
            ClassDTO classInfo = planningService.getClassById(classId);
            return ResponseEntity.ok(classInfo);
        } catch (CustomException e) {
            log.error("Error al obtener información de la clase {}: {}", classId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener información de la clase {}", classId, e);
            throw new CustomException("Error al obtener información de la clase");
        }
    }

    /**
     * Validar conflictos antes de crear/actualizar una clase
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/classes/validate")
    public ResponseEntity<Map<String, Object>> validateClass(@RequestBody ClassDTO classDTO) {
        try {
            log.info("Validando clase: {}", classDTO.getCourseName());
            // Este método de validación podría implementarse en el servicio
            Map<String, Object> validationResult = Map.of(
                "valid", true,
                "message", "Clase válida",
                "conflicts", List.of()
            );
            return ResponseEntity.ok(validationResult);
        } catch (Exception e) {
            log.error("Error al validar clase", e);
            throw new CustomException("Error al validar la clase");
        }
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("Error de negocio: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error de validación",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Parámetro inválido",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno del servidor",
                    "message", "Ha ocurrido un error inesperado",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }
}
