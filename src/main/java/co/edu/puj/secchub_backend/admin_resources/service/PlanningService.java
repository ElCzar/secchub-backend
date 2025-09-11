package co.edu.puj.secchub_backend.admin_resources.service;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.ClassScheduleDTO;
import co.edu.puj.secchub_backend.admin_resources.dto.TeacherDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.model.AcademicClass;
import co.edu.puj.secchub_backend.admin_resources.model.ClassSchedule;
import co.edu.puj.secchub_backend.admin_resources.model.Teacher;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassRepository;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassScheduleRepository;
import co.edu.puj.secchub_backend.admin_resources.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing academic planning operations.
 * 
 * <p>This service provides comprehensive functionality for academic planning
 * including class management, schedule creation, conflict detection, and
 * resource optimization. It serves as the business logic layer for the
 * academic planning module (HU07) in the admin_resources context.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Academic class CRUD operations</li>
 * <li>Class schedule management and conflict detection</li>
 * <li>Teacher management and availability tracking</li>
 * <li>Resource optimization and assignment validation</li>
 * <li>Business rule enforcement for academic planning</li>
 * <li>Data transformation between entities and DTOs</li>
 * </ul></p>
 * 
 * <p>This service ensures data integrity, enforces business rules,
 * and provides a clean API for the planning controllers.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningService {
    
    private final ClassRepository classRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Creates a new academic class with validation and business rule enforcement.
     * 
     * <p>This method performs comprehensive validation including:
     * <ul>
     * <li>Class data integrity validation</li>
     * <li>Capacity constraints validation</li>
     * <li>Date range consistency checks</li>
     * <li>Duplicate class detection</li>
     * </ul></p>
     * 
     * @param classDTO the class data transfer object containing class information
     * @return the created class as a DTO
     * @throws CustomException if validation fails or duplicate class exists
     */
    @Transactional
    public ClassDTO createClass(ClassDTO classDTO) {
        log.info("Creando nueva clase: {}", classDTO.getCourseName());
        
        validateClassData(classDTO);
        validateClassCapacity(classDTO);
        validateDateRange(classDTO.getStartDate(), classDTO.getEndDate());
        
        if (existsDuplicateClass(classDTO)) {
            throw new CustomException("Ya existe una clase para este curso en el semestre especificado");
        }
        
        AcademicClass academicClass = modelMapper.map(classDTO, AcademicClass.class);
        AcademicClass savedClass = classRepository.save(academicClass);
        
        log.info("Clase creada exitosamente con ID: {}", savedClass.getId());
        return modelMapper.map(savedClass, ClassDTO.class);
    }
    
    /**
     * Obtener todas las clases de un semestre
     */
    public Page<ClassDTO> getClassesBySemester(Long semesterId, Pageable pageable) {
        log.info("Obteniendo clases del semestre: {}", semesterId);
        
        Page<AcademicClass> classes = classRepository.findBySemesterId(semesterId, pageable);
        return classes.map(academicClass -> {
            ClassDTO dto = modelMapper.map(academicClass, ClassDTO.class);
            loadAdditionalClassInfo(dto);
            return dto;
        });
    }
    
    /**
     * Obtener todas las clases de un semestre (sin paginación)
     */
    public List<ClassDTO> getClassesBySemester(Long semesterId) {
        log.info("Obteniendo clases del semestre: {} (sin paginación)", semesterId);
        
        List<AcademicClass> classes = classRepository.findBySemesterId(semesterId);
        return classes.stream().map(academicClass -> {
            ClassDTO dto = modelMapper.map(academicClass, ClassDTO.class);
            loadAdditionalClassInfo(dto);
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Obtener todas las clases
     */
    public List<ClassDTO> getAllClasses() {
        log.info("Obteniendo todas las clases");
        
        List<AcademicClass> classes = classRepository.findAll();
        return classes.stream().map(academicClass -> {
            ClassDTO dto = modelMapper.map(academicClass, ClassDTO.class);
            loadAdditionalClassInfo(dto);
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Obtener una clase por ID
     */
    public ClassDTO getClassById(Long classId) {
        log.info("Obteniendo clase por ID: {}", classId);
        
        AcademicClass academicClass = classRepository.findById(classId)
            .orElseThrow(() -> new CustomException("Clase no encontrada con ID: " + classId));
            
        ClassDTO dto = modelMapper.map(academicClass, ClassDTO.class);
        loadAdditionalClassInfo(dto);
        return dto;
    }
    
    /**
     * Actualizar una clase existente
     */
    @Transactional
    public ClassDTO updateClass(Long classId, ClassDTO classDTO) {
        log.info("Actualizando clase con ID: {}", classId);
        
        AcademicClass existingClass = classRepository.findById(classId)
            .orElseThrow(() -> new CustomException("Clase no encontrada con ID: " + classId));
        
        validateClassData(classDTO);
        validateDateRange(classDTO.getStartDate(), classDTO.getEndDate());
        
        updateClassFields(existingClass, classDTO);
        
        AcademicClass updatedClass = classRepository.save(existingClass);
        log.info("Clase actualizada exitosamente");
        
        return modelMapper.map(updatedClass, ClassDTO.class);
    }
    
    /**
     * Eliminar una clase y todos sus horarios asociados
     */
    @Transactional
    public void deleteClass(Long classId) {
        log.info("Eliminando clase con ID: {}", classId);
        
        if (!classRepository.existsById(classId)) {
            throw new CustomException("Clase no encontrada con ID: " + classId);
        }
        
        validateClassDeletion(classId);
        classScheduleRepository.deleteByClassId(classId);
        classRepository.deleteById(classId);
        
        log.info("Clase eliminada exitosamente");
    }
    
    /**
     * Duplicar planificación de un semestre anterior
     */
    @Transactional
    public List<ClassDTO> duplicateSemesterPlanning(Long sourceSemesterId, Long targetSemesterId) {
        log.info("Duplicando planificación del semestre {} al semestre {}", sourceSemesterId, targetSemesterId);
        
        validateTargetSemesterEmpty(targetSemesterId);
        
        List<AcademicClass> sourceClasses = classRepository.findBySemesterId(sourceSemesterId);
        
        if (sourceClasses.isEmpty()) {
            throw new CustomException("No hay clases para duplicar en el semestre origen");
        }
        
        List<AcademicClass> duplicatedClasses = sourceClasses.stream()
            .map(sourceClass -> duplicateClass(sourceClass, targetSemesterId))
            .collect(Collectors.toList());
        
        List<AcademicClass> savedClasses = classRepository.saveAll(duplicatedClasses);
        
        for (int i = 0; i < sourceClasses.size(); i++) {
            duplicateClassSchedules(sourceClasses.get(i).getId(), savedClasses.get(i).getId());
        }
        
        log.info("Planificación duplicada exitosamente. {} clases creadas", savedClasses.size());
        
        return savedClasses.stream()
            .map(academicClass -> modelMapper.map(academicClass, ClassDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Asignar horario a una clase
     */
    @Transactional
    public ClassScheduleDTO assignScheduleToClass(Long classId, ClassScheduleDTO scheduleDTO) {
        log.info("Asignando horario a la clase con ID: {}", classId);
        
        if (!classRepository.existsById(classId)) {
            throw new CustomException("Clase no encontrada con ID: " + classId);
        }
        
        validateScheduleData(scheduleDTO);
        validateTimeRange(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());
        validateNoScheduleConflicts(scheduleDTO);
        
        scheduleDTO.setClassId(classId);
        ClassSchedule schedule = modelMapper.map(scheduleDTO, ClassSchedule.class);
        ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
        
        log.info("Horario asignado exitosamente");
        return modelMapper.map(savedSchedule, ClassScheduleDTO.class);
    }
    
    /**
     * Obtener horarios de una clase
     */
    public List<ClassScheduleDTO> getClassSchedules(Long classId) {
        log.info("Obteniendo horarios de la clase: {}", classId);
        
        List<ClassSchedule> schedules = classScheduleRepository.findByClassIdOrderedByDayAndTime(classId);
        return schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ClassScheduleDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener profesores disponibles para una clase
     */
    public List<TeacherDTO> getAvailableTeachersForClass(Integer requiredHours) {
        log.info("Buscando profesores disponibles con {} horas", requiredHours);
        
        List<Teacher> availableTeachers = teacherRepository.findTeachersAvailableForHours(requiredHours);
        
        return availableTeachers.stream()
            .map(teacher -> modelMapper.map(teacher, TeacherDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Detectar conflictos de horarios
     */
    public List<ClassScheduleDTO> detectScheduleConflicts(Long classroomId, String day) {
        log.info("Detectando conflictos de horario en aula {} para el día {}", classroomId, day);
        
        List<ClassSchedule> schedules = classScheduleRepository.findByDayAndClassroomId(day, classroomId);
        
        return schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ClassScheduleDTO.class))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas de utilización
     */
    public Map<String, Object> getClassroomUtilizationStats(Long semesterId) {
        log.info("Calculando estadísticas de utilización para semestre: {}", semesterId);
        
        List<AcademicClass> classes = classRepository.findBySemesterId(semesterId);
        
        return Map.of(
            "totalClasses", classes.size(),
            "averageClassCapacity", calculateAverageCapacity(classes)
        );
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==========================================
    
    private void validateClassData(ClassDTO classDTO) {
        if (classDTO.getCourseId() == null) {
            throw new CustomException("El ID del curso es obligatorio");
        }
        if (classDTO.getSemesterId() == null) {
            throw new CustomException("El ID del semestre es obligatorio");
        }
        if (classDTO.getCapacity() == null || classDTO.getCapacity() <= 0) {
            throw new CustomException("La capacidad debe ser mayor a 0");
        }
    }
    
    private void validateClassCapacity(ClassDTO classDTO) {
        if (classDTO.getCapacity() > 100) {
            throw new CustomException("La capacidad no puede exceder 100 estudiantes");
        }
    }
    
    private void validateDateRange(Date startDate, Date endDate) {
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new CustomException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
    
    private boolean existsDuplicateClass(ClassDTO classDTO) {
        return classRepository.existsByCourseIdAndSemesterIdAndStatusId(
            classDTO.getCourseId(), 
            classDTO.getSemesterId(), 
            1L
        );
    }
    
    private void validateScheduleData(ClassScheduleDTO scheduleDTO) {
        if (scheduleDTO.getDay() == null || scheduleDTO.getDay().trim().isEmpty()) {
            throw new CustomException("El día es obligatorio");
        }
        if (scheduleDTO.getStartTime() == null || scheduleDTO.getEndTime() == null) {
            throw new CustomException("Las horas de inicio y fin son obligatorias");
        }
        if (scheduleDTO.getClassroomId() == null) {
            throw new CustomException("El ID del aula es obligatorio");
        }
    }
    
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new CustomException("La hora de inicio no puede ser posterior a la hora de fin");
        }
    }
    
    private void validateNoScheduleConflicts(ClassScheduleDTO scheduleDTO) {
        boolean hasConflict = classScheduleRepository.existsConflictingSchedule(
            scheduleDTO.getClassroomId(),
            scheduleDTO.getDay(),
            scheduleDTO.getStartTime(),
            scheduleDTO.getEndTime(),
            scheduleDTO.getClassId() != null ? scheduleDTO.getClassId() : 0L
        );
        
        if (hasConflict) {
            throw new CustomException("Existe un conflicto de horario en el aula especificada");
        }
    }
    
    private void loadAdditionalClassInfo(ClassDTO dto) {
        // TODO: Cargar información de curso, semestre, etc.
    }
    
    private void updateClassFields(AcademicClass existingClass, ClassDTO classDTO) {
        existingClass.setCapacity(classDTO.getCapacity());
        existingClass.setObservation(classDTO.getObservation());
        
        if (classDTO.getStartDate() != null) {
            existingClass.setStartDate(convertToLocalDate(classDTO.getStartDate()));
        }
        if (classDTO.getEndDate() != null) {
            existingClass.setEndDate(convertToLocalDate(classDTO.getEndDate()));
        }
    }
    
    private void validateClassDeletion(Long classId) {
        // TODO: Verificar que no tenga asignaciones activas
    }
    
    private void validateTargetSemesterEmpty(Long semesterId) {
        long existingClasses = classRepository.countBySemesterId(semesterId);
        if (existingClasses > 0) {
            throw new CustomException("El semestre destino ya tiene clases planificadas");
        }
    }
    
    private AcademicClass duplicateClass(AcademicClass sourceClass, Long targetSemesterId) {
        return AcademicClass.builder()
            .courseId(sourceClass.getCourseId())
            .semesterId(targetSemesterId)
            .startDate(sourceClass.getStartDate())
            .endDate(sourceClass.getEndDate())
            .capacity(sourceClass.getCapacity())
            .observation(sourceClass.getObservation())
            .statusId(sourceClass.getStatusId())
            .build();
    }
    
    private void duplicateClassSchedules(Long sourceClassId, Long targetClassId) {
        List<ClassSchedule> sourceSchedules = classScheduleRepository.findByClassId(sourceClassId);
        
        List<ClassSchedule> duplicatedSchedules = sourceSchedules.stream()
            .map(schedule -> ClassSchedule.builder()
                .classId(targetClassId)
                .classroomId(schedule.getClassroomId())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .modalityId(schedule.getModalityId())
                .disability(schedule.getDisability())
                .build())
            .collect(Collectors.toList());
        
        classScheduleRepository.saveAll(duplicatedSchedules);
    }
    
    private double calculateAverageCapacity(List<AcademicClass> classes) {
        return classes.stream()
            .mapToInt(AcademicClass::getCapacity)
            .average()
            .orElse(0.0);
    }
    
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }
}
