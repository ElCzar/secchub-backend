package co.edu.puj.secchub_backend.planning.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.ClassScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.model.Class;
import co.edu.puj.secchub_backend.planning.model.ClassSchedule;
import co.edu.puj.secchub_backend.planning.repository.ClassRepository;
import co.edu.puj.secchub_backend.planning.repository.ClassScheduleRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling planning-related operations.
 * This class manages the core business logic for the planning module.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {
    private final ModelMapper modelMapper;
    private final ClassRepository classRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AdminModuleSemesterContract semesterService;
    private final AdminModuleCourseContract courseService;
    private final JdbcTemplate jdbcTemplate;

        /**
     * Creates a new class with schedules.
     * @param classCreateRequestDTO DTO with class information
     * @return Created class DTO
     */
    @Transactional
    public Mono<ClassResponseDTO> createClass(ClassCreateRequestDTO classCreateRequestDTO) {
        return Mono.fromCallable(() -> {
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            
            // Mapear la clase base SIN horarios para evitar duplicación
            Class classEntity = modelMapper.map(classCreateRequestDTO, Class.class);
            classEntity.setSemesterId(currentSemesterId);
            classEntity.setSchedules(null); // ⚠️ IMPORTANTE: No establecer horarios aún
            
            // Guardar primero la clase para obtener el ID
            Class savedClass = classRepository.save(classEntity);
            
            // Ahora manejar horarios con el ID de la clase
            if (classCreateRequestDTO.getSchedules() != null && !classCreateRequestDTO.getSchedules().isEmpty()) {
                List<ClassSchedule> schedules = classCreateRequestDTO.getSchedules().stream()
                    .map(scheduleDTO -> {
                        ClassSchedule schedule = modelMapper.map(scheduleDTO, ClassSchedule.class);
                        schedule.setClassId(savedClass.getId()); // Usar el ID directo
                        return schedule;
                    })
                    .toList();
                
                // Guardar los horarios por separado
                List<ClassSchedule> savedSchedules = classScheduleRepository.saveAll(schedules);
                savedClass.setSchedules(savedSchedules);
            }
            
            return mapToResponseDTO(savedClass);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets all classes for the current semester.
     * @return List of classes for the current semester
     */
    public List<ClassResponseDTO> findCurrentSemesterClasses() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return classRepository.findBySemesterId(currentSemesterId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets all classes.
     * @return List of all classes
     */
    public List<ClassResponseDTO> findAllClasses() {
        return classRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets a class by ID.
     * @param classId Class ID
     * @return Class found
     */
    public Mono<ClassResponseDTO> findClassById(Long classId) {
        return Mono.fromCallable(() -> classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId)))
                .map(this::mapToResponseDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a class.
     * @param classId Class ID
     * @param classCreateRequestDTO DTO with updated data
     * @return Updated class
     */
    public Mono<ClassResponseDTO> updateClass(Long classId, ClassCreateRequestDTO classCreateRequestDTO) {
        return Mono.fromCallable(() -> {
            Class classEntity = classRepository.findById(classId)
                    .orElseThrow(() -> new ClassNotFoundException("Class not found for update with id: " + classId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(classCreateRequestDTO, classEntity);
            
            Class savedClass = classRepository.save(classEntity);
            return mapToResponseDTO(savedClass);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a class by ID.
     * @param classId Class ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClass(Long classId) {
        return Mono.fromCallable(() -> {
            if (!classRepository.existsById(classId)) {
                throw new ClassNotFoundException("Class not found for deletion with id: " + classId);
            }
            classRepository.deleteById(classId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Gets classes by course ID.
     * @param courseId Course ID
     * @return List of classes for the specified course
     */
    public List<ClassResponseDTO> findClassesByCourse(Long courseId) {
        return classRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets classes by section.
     * @param section Section number
     * @return List of classes for the specified section
     */
    public List<ClassResponseDTO> findClassesBySection(Long section) {
        return classRepository.findBySection(section).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets classes by semester and course for the current semester.
     * @param courseId Course ID
     * @return List of classes for the current semester and specified course
     */
    public List<ClassResponseDTO> findCurrentSemesterClassesByCourse(Long courseId) {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return classRepository.findBySemesterIdAndCourseId(currentSemesterId, courseId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Adds a schedule to a class.
     * @param classId Class ID
     * @param classScheduleRequestDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<ClassScheduleResponseDTO> addClassSchedule(Long classId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId));

            ClassSchedule schedule = modelMapper.map(classScheduleRequestDTO, ClassSchedule.class);
            schedule.setClassId(classId);

            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules associated with a class.
     * @param classId Class ID
     * @return List of schedules
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByClassId(Long classId) {
        classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException("Class not found for retrieval with id: " + classId));

        List<ClassSchedule> classSchedules = classScheduleRepository.findByClassId(classId);
        return classSchedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets a class schedule by ID.
     * @param scheduleId Schedule ID
     * @return Class schedule found
     */
    public Mono<ClassScheduleResponseDTO> findClassScheduleById(Long scheduleId) {
        return Mono.fromCallable(() -> classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for retrieval with id: " + scheduleId)))
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a specific class schedule.
     * @param scheduleId Schedule ID
     * @param classScheduleRequestDTO DTO with updated data
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> updateClassSchedule(Long scheduleId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for update with id: " + scheduleId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(classScheduleRequestDTO, schedule);

            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a class schedule by ID.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClassSchedule(Long scheduleId) {
        return Mono.fromCallable(() -> {
            if (!classScheduleRepository.existsById(scheduleId)) {
                throw new ClassScheduleNotFoundException("Class schedule not found for deletion with id: " + scheduleId);
            }
            classScheduleRepository.deleteById(scheduleId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Partially updates a class schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> patchClassSchedule(Long scheduleId, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new ClassScheduleNotFoundException("Class schedule not found for retrieval with id: " + scheduleId));

            ClassScheduleResponseDTO updateDTO = new ClassScheduleResponseDTO();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "startTime" -> {
                        if (value instanceof String stringValue) {
                            updateDTO.setStartTime(LocalTime.parse(stringValue));
                        } else if (value instanceof LocalTime localTimeValue) {
                            updateDTO.setStartTime(localTimeValue);
                        }
                    }
                    case "endTime" -> {
                        if (value instanceof String stringValue) {
                            updateDTO.setEndTime(LocalTime.parse(stringValue));
                        } else if (value instanceof LocalTime localTimeValue) {
                            updateDTO.setEndTime(localTimeValue);
                        }
                    }
                    case "day" -> updateDTO.setDay((String) value);
                    case "classroomId" -> updateDTO.setClassroomId((Long) value);
                    case "modalityId" -> updateDTO.setModalityId((Long) value);
                    case "disability" -> updateDTO.setDisability((Boolean) value);
                    default -> {}
                }
            });

            modelMapper.map(updateDTO, schedule);
            ClassSchedule savedSchedule = classScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules by classroom ID.
     * @param classroomId Classroom ID
     * @return List of schedules for the specified classroom
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByClassroom(Long classroomId) {
        List<ClassSchedule> schedules = classScheduleRepository.findByClassroomId(classroomId);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets schedules by day.
     * @param day Day of the week
     * @return List of schedules for the specified day
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByDay(String day) {
        List<ClassSchedule> schedules = classScheduleRepository.findByDay(day);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Gets schedules with disability accommodations.
     * @param disability True to find schedules with disability accommodations
     * @return List of schedules with disability considerations
     */
    public List<ClassScheduleResponseDTO> findClassSchedulesByDisability(Boolean disability) {
        List<ClassSchedule> schedules = classScheduleRepository.findByDisability(disability);
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Find classes by semester id (explicit endpoint used by frontend)
     */
    public List<ClassResponseDTO> findClassesBySemester(Long semesterId) {
        return classRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Basic validation of a class before creation/update. Returns map with conflicts and message.
     */
    public Mono<Map<String, Object>> validateClass(ClassCreateRequestDTO classCreateRequestDTO) {
        return Mono.fromCallable(() -> {
            // Simple validation: check schedule conflicts for each provided schedule
            var conflicts = new java.util.ArrayList<Map<String, Object>>();

            if (classCreateRequestDTO.getSchedules() != null) {
                for (var sched : classCreateRequestDTO.getSchedules()) {
                    var overlapping = classScheduleRepository.findConflictingSchedules(
                            sched.getClassroomId(), sched.getDay(), sched.getStartTime(), sched.getEndTime());
                    if (!overlapping.isEmpty()) {
                        overlapping.forEach(o -> conflicts.add(Map.of(
                                "existingScheduleId", o.getId(),
                                "day", o.getDay(),
                                "startTime", o.getStartTime(),
                                "endTime", o.getEndTime(),
                                "classroomId", o.getClassroomId()
                        )));
                    }
                }
            }

            return Map.<String, Object>of(
                    "valid", conflicts.isEmpty(),
                    "message", conflicts.isEmpty() ? "No conflicts" : "Found conflicts",
                    "conflicts", conflicts
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Find conflicting schedules for a classroom and day
     */
    public List<ClassScheduleResponseDTO> findConflictingSchedulesByClassroomAndDay(Long classroomId, String day) {
        // Return schedules for that classroom and day as potential conflicts
        List<ClassSchedule> schedules = classScheduleRepository.findByClassroomId(classroomId).stream()
                .filter(s -> day.equalsIgnoreCase(s.getDay()))
                .toList();
        return schedules.stream()
                .map(s -> modelMapper.map(s, ClassScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Duplicate planning from source semester to target semester
     */
    @Transactional
    public List<ClassResponseDTO> duplicateSemesterPlanning(Long sourceSemesterId, Long targetSemesterId) {
        List<Class> sourceClasses = classRepository.findBySemesterId(sourceSemesterId);
        var created = new java.util.ArrayList<ClassResponseDTO>();

        for (Class src : sourceClasses) {
            Class copy = new Class();
            modelMapper.map(src, copy);
            copy.setId(null); // new entity
            copy.setSemesterId(targetSemesterId);
            Class saved = classRepository.save(copy);

            // duplicate schedules
            List<ClassSchedule> schedules = classScheduleRepository.findByClassId(src.getId());
            for (ClassSchedule s : schedules) {
                ClassSchedule sCopy = new ClassSchedule();
                modelMapper.map(s, sCopy);
                sCopy.setId(null);
                sCopy.setClassId(saved.getId());
                classScheduleRepository.save(sCopy);
            }

            created.add(mapToResponseDTO(saved));
        }

        return created;
    }

    /**
     * Basic utilization statistics for semester
     */
    public Map<String, Object> getUtilizationStatistics(Long semesterId) {
        List<Class> classes = classRepository.findBySemesterId(semesterId);
        int totalClasses = classes.size();
        int totalSeats = classes.stream().mapToInt(c -> c.getCapacity() == null ? 0 : c.getCapacity()).sum();

        return Map.of(
                "semesterId", semesterId,
                "totalClasses", totalClasses,
                "totalSeats", totalSeats
        );
    }

    /**
     * Simple stub for available teachers (frontend uses it). In real app, delegate to teacher service.
     */
    public List<Map<String, Object>> getAvailableTeachers(Integer requiredHours) {
        // Query both users and teacher tables to get only valid teachers
        String sql = """
            SELECT t.id as teacher_id, u.name, u.last_name, u.email, t.max_hours
            FROM teacher t 
            JOIN users u ON t.user_id = u.id 
            WHERE u.role_id = 4
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows.stream().map(r -> Map.<String, Object>of(
            "id", r.get("teacher_id"),  // Use teacher_id instead of user_id
            "name", r.get("name"),
            "lastName", r.get("last_name"),
            "email", r.get("email"),
            "availableHours", r.get("max_hours") != null ? r.get("max_hours") : 0
        )).toList();
    }
    
    private ClassResponseDTO mapToResponseDTO(Class classEntity) {
    // Manual mapping to avoid lazy initialization issues when ModelMapper tries to map collections
    ClassResponseDTO responseDTO = new ClassResponseDTO();
    responseDTO.setId(classEntity.getId());
    responseDTO.setSection(classEntity.getSection());
    responseDTO.setCourseId(classEntity.getCourseId());
    
    // Get course name using the admin module contract
    String courseName = courseService.getCourseName(classEntity.getCourseId());
    responseDTO.setCourseName(courseName);
    
    responseDTO.setSemesterId(classEntity.getSemesterId());
    responseDTO.setStartDate(classEntity.getStartDate());
    responseDTO.setEndDate(classEntity.getEndDate());
    responseDTO.setObservation(classEntity.getObservation());
    responseDTO.setCapacity(classEntity.getCapacity());
    responseDTO.setStatusId(classEntity.getStatusId());

    // Load schedules explicitly from repository (avoids LazyInitializationException)
    List<ClassSchedule> schedules = classScheduleRepository.findByClassId(classEntity.getId());
    List<ClassScheduleResponseDTO> scheduleDTOs = schedules.stream()
        .map(schedule -> modelMapper.map(schedule, ClassScheduleResponseDTO.class))
        .toList();

    responseDTO.setSchedules(scheduleDTOs);
    return responseDTO;
    }

    /**
     * Find classes by semester ID
     */
    public List<ClassResponseDTO> findClassesBySemesterId(Long semesterId) {
        List<Class> classes = classRepository.findBySemesterId(semesterId);
        return classes.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Get past semesters available for planning duplication
     */
    public List<Map<String, Object>> getPastSemesters() {
        return semesterService.getPastSemesters();
    }

    /**
     * Apply planning from source semester to current semester
     */
    @Transactional
    public Map<String, Object> applySemesterPlanningToCurrent(Long sourceSemesterId) {
        try {
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            
            // Get classes from source semester
            List<Class> sourceClasses = classRepository.findBySemesterId(sourceSemesterId);
            
            int copiedClasses = 0;
            int copiedSchedules = 0;
            
            for (Class sourceClass : sourceClasses) {
                // Create new class for current semester
                Class newClass = Class.builder()
                        .section(sourceClass.getSection())
                        .courseId(sourceClass.getCourseId())
                        .semesterId(currentSemesterId)
                        .startDate(sourceClass.getStartDate())
                        .endDate(sourceClass.getEndDate())
                        .observation(sourceClass.getObservation() + " (Copiado del semestre " + sourceSemesterId + ")")
                        .capacity(sourceClass.getCapacity())
                        .statusId(sourceClass.getStatusId())
                        .build();
                
                // Save the class first
                Class savedClass = classRepository.save(newClass);
                copiedClasses++;
                
                // Copy schedules
                List<ClassSchedule> sourceSchedules = classScheduleRepository.findByClassId(sourceClass.getId());
                for (ClassSchedule sourceSchedule : sourceSchedules) {
                    ClassSchedule newSchedule = ClassSchedule.builder()
                            .classId(savedClass.getId())
                            .classroomId(sourceSchedule.getClassroomId())
                            .day(sourceSchedule.getDay())
                            .startTime(sourceSchedule.getStartTime())
                            .endTime(sourceSchedule.getEndTime())
                            .modalityId(sourceSchedule.getModalityId())
                            .disability(sourceSchedule.getDisability())
                            .build();
                    
                    classScheduleRepository.save(newSchedule);
                    copiedSchedules++;
                }
            }
            
            return Map.of(
                "success", true,
                "message", "Planificación aplicada exitosamente",
                "sourceSemesterId", sourceSemesterId,
                "targetSemesterId", currentSemesterId,
                "copiedClasses", copiedClasses,
                "copiedSchedules", copiedSchedules
            );
            
        } catch (Exception e) {
            // Error applying semester planning
            return Map.of(
                "success", false,
                "message", "Error aplicando planificación: " + e.getMessage(),
                "error", e.getMessage()
            );
        }
    }
}
