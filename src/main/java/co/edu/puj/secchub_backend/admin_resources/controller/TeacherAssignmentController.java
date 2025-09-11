package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.TeacherDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.service.TeacherAssignmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for teacher assignment management.
 * 
 * <p>This controller provides comprehensive endpoints for managing teacher
 * assignments to academic classes. It handles the complete assignment lifecycle
 * including creation, teacher responses, workload management, and assignment
 * analytics. It serves as the API layer for teacher assignment operations
 * in the academic planning module.</p>
 * 
 * <p>Available endpoints include:
 * <ul>
 * <li>Teacher-to-class assignment operations</li>
 * <li>Assignment status management (pending, accepted, rejected)</li>
 * <li>Teacher response handling and decision tracking</li>
 * <li>Workload calculation and optimization</li>
 * <li>Assignment analytics and reporting</li>
 * <li>Bulk assignment operations</li>
 * </ul></p>
 * 
 * <p>All endpoints enforce business rules for workload limits, prevent
 * assignment conflicts, and ensure proper teacher capacity management.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/teacher-assignments")
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    // ==========================================
    // GESTIÓN DE ASIGNACIONES
    // ==========================================

    /**
     * Assigns a teacher to an academic class.
     * 
     * <p>This endpoint creates a new assignment between a teacher and a class,
     * performing workload validation, capacity checks, and conflict detection.
     * The assignment is initially created in pending status awaiting teacher response.</p>
     * 
     * @param teacherId the ID of the teacher to assign
     * @param classId the ID of the academic class
     * @param workHours the number of work hours for this assignment
     * @return ResponseEntity containing the updated teacher DTO and HTTP 200 status
     * @throws CustomException if assignment validation fails or conflicts exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/assign")
    public ResponseEntity<TeacherDTO> assignTeacherToClass(
            @RequestParam Long teacherId,
            @RequestParam Long classId,
            @RequestParam Integer workHours,
            @RequestParam(required = false) String observation) {
        try {
            log.info("Asignando profesor {} a clase {} con {} horas", teacherId, classId, workHours);
            TeacherDTO assignment = teacherAssignmentService.assignTeacherToClass(
                    teacherId, classId, workHours, observation);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (CustomException e) {
            log.error("Error al asignar profesor: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al asignar profesor", e);
            throw new CustomException("Error interno del servidor al asignar profesor");
        }
    }

    /**
     * Actualizar una asignación existente
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{assignmentId}")
    public ResponseEntity<TeacherDTO> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestParam Integer workHours,
            @RequestParam(required = false) String observation) {
        try {
            log.info("Actualizando asignación {}", assignmentId);
            TeacherDTO updatedAssignment = teacherAssignmentService.updateTeacherAssignment(
                    assignmentId, workHours, observation);
            return ResponseEntity.ok(updatedAssignment);
        } catch (CustomException e) {
            log.error("Error al actualizar asignación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar asignación", e);
            throw new CustomException("Error interno del servidor al actualizar asignación");
        }
    }

    /**
     * Eliminar una asignación
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long assignmentId) {
        try {
            log.info("Eliminando asignación {}", assignmentId);
            teacherAssignmentService.removeTeacherAssignment(assignmentId);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            log.error("Error al eliminar asignación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar asignación", e);
            throw new CustomException("Error interno del servidor al eliminar asignación");
        }
    }

    // ==========================================
    // CONSULTAS DE ASIGNACIONES
    // ==========================================

    /**
     * Obtener profesores asignados a una clase
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/class/{classId}/teachers")
    public ResponseEntity<List<TeacherDTO>> getTeachersAssignedToClass(@PathVariable Long classId) {
        try {
            log.info("Obteniendo profesores asignados a la clase: {}", classId);
            List<TeacherDTO> teachers = teacherAssignmentService.getTeachersAssignedToClass(classId);
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error al obtener profesores de la clase {}", classId, e);
            throw new CustomException("Error al obtener profesores asignados a la clase");
        }
    }

    /**
     * Obtener clases asignadas a un profesor
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/teacher/{teacherId}/classes")
    public ResponseEntity<List<ClassDTO>> getClassesAssignedToTeacher(@PathVariable Long teacherId) {
        try {
            log.info("Obteniendo clases asignadas al profesor: {}", teacherId);
            List<ClassDTO> classes = teacherAssignmentService.getClassesAssignedToTeacher(teacherId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Error al obtener clases del profesor {}", teacherId, e);
            throw new CustomException("Error al obtener clases asignadas al profesor");
        }
    }

    /**
     * Obtener profesores disponibles para una clase
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/class/{classId}/available-teachers")
    public ResponseEntity<List<TeacherDTO>> getAvailableTeachersForClass(
            @PathVariable Long classId,
            @RequestParam Integer requiredHours) {
        try {
            log.info("Buscando profesores disponibles para clase {} con {} horas", classId, requiredHours);
            List<TeacherDTO> availableTeachers = teacherAssignmentService.getAvailableTeachersForClass(
                    classId, requiredHours);
            return ResponseEntity.ok(availableTeachers);
        } catch (Exception e) {
            log.error("Error al obtener profesores disponibles", e);
            throw new CustomException("Error al obtener profesores disponibles");
        }
    }

    // ==========================================
    // GESTIÓN DE DECISIONES (HU17)
    // ==========================================

    /**
     * Profesor acepta una asignación
     */
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<TeacherDTO> acceptAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String observation) {
        try {
            log.info("Aceptando asignación {}", assignmentId);
            TeacherDTO acceptedAssignment = teacherAssignmentService.acceptAssignment(assignmentId, observation);
            return ResponseEntity.ok(acceptedAssignment);
        } catch (CustomException e) {
            log.error("Error al aceptar asignación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al aceptar asignación", e);
            throw new CustomException("Error interno del servidor al aceptar asignación");
        }
    }

    /**
     * Profesor rechaza una asignación
     */
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<TeacherDTO> rejectAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String observation) {
        try {
            log.info("Rechazando asignación {}", assignmentId);
            TeacherDTO rejectedAssignment = teacherAssignmentService.rejectAssignment(assignmentId, observation);
            return ResponseEntity.ok(rejectedAssignment);
        } catch (CustomException e) {
            log.error("Error al rechazar asignación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al rechazar asignación", e);
            throw new CustomException("Error interno del servidor al rechazar asignación");
        }
    }

    /**
     * Obtener asignaciones pendientes de un profesor
     */
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/teacher/{teacherId}/pending")
    public ResponseEntity<List<TeacherDTO>> getPendingAssignments(@PathVariable Long teacherId) {
        try {
            log.info("Obteniendo asignaciones pendientes del profesor: {}", teacherId);
            List<TeacherDTO> pendingAssignments = teacherAssignmentService.getPendingAssignments(teacherId);
            return ResponseEntity.ok(pendingAssignments);
        } catch (Exception e) {
            log.error("Error al obtener asignaciones pendientes del profesor {}", teacherId, e);
            throw new CustomException("Error al obtener asignaciones pendientes");
        }
    }

    // ==========================================
    // REPORTES Y ESTADÍSTICAS
    // ==========================================

    /**
     * Obtener reporte de carga horaria por profesor
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/workload-report")
    public ResponseEntity<List<TeacherDTO>> getWorkloadReport() {
        try {
            log.info("Generando reporte de carga horaria");
            List<TeacherDTO> workloadReport = teacherAssignmentService.getTeacherWorkloadReport();
            return ResponseEntity.ok(workloadReport);
        } catch (Exception e) {
            log.error("Error al generar reporte de carga horaria", e);
            throw new CustomException("Error al generar reporte de carga horaria");
        }
    }

    /**
     * Obtener estadísticas generales de asignaciones
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAssignmentStatistics() {
        try {
            log.info("Obteniendo estadísticas de asignaciones");
            Map<String, Object> statistics = teacherAssignmentService.getAssignmentStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de asignaciones", e);
            throw new CustomException("Error al obtener estadísticas de asignaciones");
        }
    }

    // ==========================================
    // MANEJO DE ERRORES
    // ==========================================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        log.error("Error de negocio en asignaciones: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error de validación",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Argumento inválido en asignaciones: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Parámetro inválido",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado en asignaciones", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error interno del servidor",
                    "message", "Ha ocurrido un error inesperado en las asignaciones",
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
    }
}
