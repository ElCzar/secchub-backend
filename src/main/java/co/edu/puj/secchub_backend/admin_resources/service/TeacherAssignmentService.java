package co.edu.puj.secchub_backend.admin_resources.service;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.TeacherDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.model.AcademicClass;
import co.edu.puj.secchub_backend.admin_resources.model.Teacher;
import co.edu.puj.secchub_backend.admin_resources.model.TeacherAssignment;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassRepository;
import co.edu.puj.secchub_backend.admin_resources.repository.TeacherRepository;
import co.edu.puj.secchub_backend.admin_resources.repository.TeacherAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing teacher assignments to academic classes.
 * 
 * <p>This service provides comprehensive functionality for teacher assignment
 * management including assignment creation, workload optimization, teacher
 * response handling, and business rule enforcement. It serves as the core
 * business logic for teacher-class relationships in the academic planning module.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Teacher assignment creation and validation</li>
 * <li>Workload calculation and distribution optimization</li>
 * <li>Teacher response management (accept/reject assignments)</li>
 * <li>Assignment status tracking and updates</li>
 * <li>Capacity and availability constraint enforcement</li>
 * <li>Assignment analytics and reporting</li>
 * </ul></p>
 * 
 * <p>The service ensures proper workload distribution, prevents overassignment,
 * and maintains assignment integrity throughout the academic planning process.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {
    
    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final ModelMapper modelMapper;
    
    // Constantes para estados de asignación
    private static final Long STATUS_PENDING = 1L;
    private static final Long STATUS_ACCEPTED = 2L;
    private static final Long STATUS_REJECTED = 3L;
    
    // ==========================================
    // GESTIÓN DE ASIGNACIONES
    // ==========================================
    
    /**
     * Asignar un profesor a una clase
     */
    @Transactional
    public TeacherDTO assignTeacherToClass(Long teacherId, Long classId, Integer workHours, String observation) {
        log.info("Asignando profesor {} a clase {} con {} horas", teacherId, classId, workHours);
        
        // Validar que el profesor y la clase existan
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new CustomException("Profesor no encontrado con ID: " + teacherId));
        
        if (!classRepository.existsById(classId)) {
            throw new CustomException("Clase no encontrada con ID: " + classId);
        }
        
        // Validar disponibilidad del profesor
        validateTeacherAvailability(teacherId, workHours);
        
        // Verificar que no exista una asignación previa
        if (teacherAssignmentRepository.findByTeacherIdAndClassId(teacherId, classId).isPresent()) {
            throw new CustomException("El profesor ya está asignado a esta clase");
        }
        
        // Calcular horas extra según tipo de empleo
        Map<String, Integer> extraHours = calculateExtraHours(teacher, workHours);
        
        // Crear la asignación
        TeacherAssignment assignment = TeacherAssignment.builder()
            .teacherId(teacherId)
            .classId(classId)
            .workHours(workHours)
            .fullTimeExtraHours(extraHours.get("fullTimeExtra"))
            .adjunctExtraHours(extraHours.get("adjunctExtra"))
            .decision(null) // Pendiente de decisión del profesor
            .observation(observation)
            .statusId(STATUS_PENDING)
            .build();
        
        TeacherAssignment savedAssignment = teacherAssignmentRepository.save(assignment);
        
        log.info("Asignación creada exitosamente con ID: {}", savedAssignment.getId());
        
        // Construir y retornar DTO con información completa
        return buildTeacherDTOWithAssignment(teacher, savedAssignment);
    }
    
    /**
     * Actualizar una asignación existente
     */
    @Transactional
    public TeacherDTO updateTeacherAssignment(Long assignmentId, Integer workHours, String observation) {
        log.info("Actualizando asignación con ID: {}", assignmentId);
        
        TeacherAssignment assignment = teacherAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new CustomException("Asignación no encontrada con ID: " + assignmentId));
        
        Teacher teacher = teacherRepository.findById(assignment.getTeacherId())
            .orElseThrow(() -> new CustomException("Profesor no encontrado"));
        
        // Validar nueva disponibilidad si cambió las horas
        if (!workHours.equals(assignment.getWorkHours())) {
            validateTeacherAvailabilityUpdate(assignment.getTeacherId(), assignmentId, workHours);
        }
        
        // Recalcular horas extra
        Map<String, Integer> extraHours = calculateExtraHours(teacher, workHours);
        
        // Actualizar asignación
        assignment.setWorkHours(workHours);
        assignment.setFullTimeExtraHours(extraHours.get("fullTimeExtra"));
        assignment.setAdjunctExtraHours(extraHours.get("adjunctExtra"));
        assignment.setObservation(observation);
        
        TeacherAssignment updatedAssignment = teacherAssignmentRepository.save(assignment);
        
        log.info("Asignación actualizada exitosamente");
        
        return buildTeacherDTOWithAssignment(teacher, updatedAssignment);
    }
    
    /**
     * Eliminar una asignación
     */
    @Transactional
    public void removeTeacherAssignment(Long assignmentId) {
        log.info("Eliminando asignación con ID: {}", assignmentId);
        
        if (!teacherAssignmentRepository.existsById(assignmentId)) {
            throw new CustomException("Asignación no encontrada con ID: " + assignmentId);
        }
        
        teacherAssignmentRepository.deleteById(assignmentId);
        
        log.info("Asignación eliminada exitosamente");
    }
    
    /**
     * Obtener profesores asignados a una clase
     */
    public List<TeacherDTO> getTeachersAssignedToClass(Long classId) {
        log.info("Obteniendo profesores asignados a la clase: {}", classId);
        
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByClassId(classId);
        
        return assignments.stream()
            .map(assignment -> {
                Teacher teacher = teacherRepository.findById(assignment.getTeacherId())
                    .orElse(null);
                if (teacher != null) {
                    return buildTeacherDTOWithAssignment(teacher, assignment);
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener clases asignadas a un profesor
     */
    public List<ClassDTO> getClassesAssignedToTeacher(Long teacherId) {
        log.info("Obteniendo clases asignadas al profesor: {}", teacherId);
        
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacherId);
        
        return assignments.stream()
            .map(assignment -> {
                AcademicClass academicClass = classRepository.findById(assignment.getClassId())
                    .orElse(null);
                if (academicClass != null) {
                    ClassDTO dto = modelMapper.map(academicClass, ClassDTO.class);
                    // Agregar información de la asignación
                    TeacherDTO teacherInfo = TeacherDTO.builder()
                        .teacherClassId(assignment.getId())
                        .workHours(assignment.getWorkHours())
                        .fullTimeExtraHours(assignment.getFullTimeExtraHours())
                        .adjunctExtraHours(assignment.getAdjunctExtraHours())
                        .decision(assignment.getDecision())
                        .observation(assignment.getObservation())
                        .assignmentStatusId(assignment.getStatusId())
                        .build();
                    dto.setTeachers(List.of(teacherInfo));
                    return dto;
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener profesores disponibles para asignar a una clase
     */
    public List<TeacherDTO> getAvailableTeachersForClass(Long classId, Integer requiredHours) {
        log.info("Buscando profesores disponibles para clase {} con {} horas", classId, requiredHours);
        
        // Obtener profesores que pueden cubrir las horas requeridas
        List<Teacher> availableTeachers = teacherRepository.findTeachersAvailableForHours(requiredHours);
        
        // Filtrar profesores ya asignados a esta clase
        List<Long> assignedTeacherIds = teacherAssignmentRepository.findByClassId(classId)
            .stream()
            .map(TeacherAssignment::getTeacherId)
            .collect(Collectors.toList());
        
        return availableTeachers.stream()
            .filter(teacher -> !assignedTeacherIds.contains(teacher.getId()))
            .map(teacher -> {
                TeacherDTO dto = modelMapper.map(teacher, TeacherDTO.class);
                // Calcular horas disponibles
                dto.setAvailableHours(calculateAvailableHours(teacher.getId(), teacher.getMaxHours()));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    // ==========================================
    // GESTIÓN DE DECISIONES DE PROFESORES (HU17)
    // ==========================================
    
    /**
     * Profesor acepta una asignación
     */
    @Transactional
    public TeacherDTO acceptAssignment(Long assignmentId, String observation) {
        log.info("Profesor acepta asignación con ID: {}", assignmentId);
        
        return updateAssignmentDecision(assignmentId, true, observation, STATUS_ACCEPTED);
    }
    
    /**
     * Profesor rechaza una asignación
     */
    @Transactional
    public TeacherDTO rejectAssignment(Long assignmentId, String observation) {
        log.info("Profesor rechaza asignación con ID: {}", assignmentId);
        
        return updateAssignmentDecision(assignmentId, false, observation, STATUS_REJECTED);
    }
    
    /**
     * Obtener asignaciones pendientes de un profesor
     */
    public List<TeacherDTO> getPendingAssignments(Long teacherId) {
        log.info("Obteniendo asignaciones pendientes del profesor: {}", teacherId);
        
        List<TeacherAssignment> pendingAssignments = teacherAssignmentRepository
            .findByTeacherIdAndStatusId(teacherId, STATUS_PENDING);
        
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new CustomException("Profesor no encontrado"));
        
        return pendingAssignments.stream()
            .map(assignment -> buildTeacherDTOWithAssignment(teacher, assignment))
            .collect(Collectors.toList());
    }
    
    // ==========================================
    // REPORTES Y ESTADÍSTICAS
    // ==========================================
    
    /**
     * Obtener reporte de carga horaria por profesor con paginación
     */
    public Page<TeacherDTO> getTeacherWorkloadReport(Pageable pageable) {
        log.info("Generando reporte de carga horaria con paginación");
        
        Page<Teacher> teachers = teacherRepository.findAll(pageable);
        return teachers.map(this::buildTeacherDTOWithWorkload);
    }
    
    /**
     * Obtener reporte de carga horaria por profesor sin paginación
     */
    public List<TeacherDTO> getTeacherWorkloadReport() {
        log.info("Generando reporte de carga horaria sin paginación");
        
        List<Teacher> teachers = teacherRepository.findAll();
        return teachers.stream()
                .map(this::buildTeacherDTOWithWorkload)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas de asignaciones
     */
    public Map<String, Object> getAssignmentStatistics() {
        log.info("Calculando estadísticas de asignaciones");
        
        List<TeacherAssignment> allAssignments = teacherAssignmentRepository.findAll();
        
        long totalAssignments = allAssignments.size();
        long pendingAssignments = allAssignments.stream()
            .filter(a -> STATUS_PENDING.equals(a.getStatusId()))
            .count();
        long acceptedAssignments = allAssignments.stream()
            .filter(a -> STATUS_ACCEPTED.equals(a.getStatusId()))
            .count();
        long rejectedAssignments = allAssignments.stream()
            .filter(a -> STATUS_REJECTED.equals(a.getStatusId()))
            .count();
        
        double acceptanceRate = totalAssignments > 0 ? 
            (double) acceptedAssignments / totalAssignments * 100 : 0;
        
        return Map.of(
            "totalAssignments", totalAssignments,
            "pendingAssignments", pendingAssignments,
            "acceptedAssignments", acceptedAssignments,
            "rejectedAssignments", rejectedAssignments,
            "acceptanceRate", acceptanceRate
        );
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN Y UTILIDAD
    // ==========================================
    
    private void validateTeacherAvailability(Long teacherId, Integer requiredHours) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new CustomException("Profesor no encontrado"));
        
        int currentHours = getCurrentAssignedHours(teacherId);
        int availableHours = teacher.getMaxHours() - currentHours;
        
        if (requiredHours > availableHours) {
            throw new CustomException(
                String.format("El profesor no tiene suficientes horas disponibles. " +
                    "Disponible: %d, Requerido: %d", availableHours, requiredHours)
            );
        }
    }
    
    private void validateTeacherAvailabilityUpdate(Long teacherId, Long excludeAssignmentId, Integer newHours) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new CustomException("Profesor no encontrado"));
        
        int currentHours = teacherAssignmentRepository.findByTeacherId(teacherId)
            .stream()
            .filter(a -> !a.getId().equals(excludeAssignmentId))
            .mapToInt(TeacherAssignment::getWorkHours)
            .sum();
        
        int availableHours = teacher.getMaxHours() - currentHours;
        
        if (newHours > availableHours) {
            throw new CustomException(
                String.format("El profesor no tiene suficientes horas disponibles. " +
                    "Disponible: %d, Requerido: %d", availableHours, newHours)
            );
        }
    }
    
    private Map<String, Integer> calculateExtraHours(Teacher teacher, Integer workHours) {
        // Lógica simplificada - ajustar según reglas de negocio específicas
        int fullTimeExtra = 0;
        int adjunctExtra = 0;
        
        // Si es profesor de tiempo completo (employmentTypeId = 1) y excede 40 horas
        if (teacher.getEmploymentTypeId() != null && teacher.getEmploymentTypeId() == 1L) {
            if (workHours > 40) {
                fullTimeExtra = workHours - 40;
            }
        }
        // Si es profesor de cátedra (employmentTypeId = 2)
        else if (teacher.getEmploymentTypeId() != null && teacher.getEmploymentTypeId() == 2L) {
            adjunctExtra = workHours; // Todas las horas son extra para cátedra
        }
        
        return Map.of(
            "fullTimeExtra", fullTimeExtra,
            "adjunctExtra", adjunctExtra
        );
    }
    
    private int getCurrentAssignedHours(Long teacherId) {
        return teacherAssignmentRepository.findByTeacherId(teacherId)
            .stream()
            .mapToInt(TeacherAssignment::getWorkHours)
            .sum();
    }
    
    private int calculateAvailableHours(Long teacherId, Integer maxHours) {
        int currentHours = getCurrentAssignedHours(teacherId);
        return maxHours - currentHours;
    }
    
    private TeacherDTO buildTeacherDTOWithAssignment(Teacher teacher, TeacherAssignment assignment) {
        TeacherDTO dto = modelMapper.map(teacher, TeacherDTO.class);
        
        // Información de la asignación
        dto.setTeacherClassId(assignment.getId());
        dto.setClassId(assignment.getClassId());
        dto.setWorkHours(assignment.getWorkHours());
        dto.setFullTimeExtraHours(assignment.getFullTimeExtraHours());
        dto.setAdjunctExtraHours(assignment.getAdjunctExtraHours());
        dto.setDecision(assignment.getDecision());
        dto.setObservation(assignment.getObservation());
        dto.setAssignmentStatusId(assignment.getStatusId());
        
        // Calcular horas totales y disponibles
        dto.setTotalHours(getCurrentAssignedHours(teacher.getId()));
        dto.setAvailableHours(calculateAvailableHours(teacher.getId(), teacher.getMaxHours()));
        
        return dto;
    }
    
    private TeacherDTO buildTeacherDTOWithWorkload(Teacher teacher) {
        TeacherDTO dto = modelMapper.map(teacher, TeacherDTO.class);
        
        // Calcular carga horaria total
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacher.getId());
        int totalHours = assignments.stream()
            .mapToInt(TeacherAssignment::getWorkHours)
            .sum();
        
        dto.setTotalHours(totalHours);
        dto.setAvailableHours(teacher.getMaxHours() - totalHours);
        
        return dto;
    }
    
    private TeacherDTO updateAssignmentDecision(Long assignmentId, Boolean decision, 
                                              String observation, Long statusId) {
        TeacherAssignment assignment = teacherAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new CustomException("Asignación no encontrada con ID: " + assignmentId));
        
        assignment.setDecision(decision);
        assignment.setObservation(observation);
        assignment.setStatusId(statusId);
        
        TeacherAssignment updatedAssignment = teacherAssignmentRepository.save(assignment);
        
        Teacher teacher = teacherRepository.findById(assignment.getTeacherId())
            .orElseThrow(() -> new CustomException("Profesor no encontrado"));
        
        log.info("Decisión de asignación actualizada: {}", decision ? "Aceptada" : "Rechazada");
        
        return buildTeacherDTOWithAssignment(teacher, updatedAssignment);
    }
}
