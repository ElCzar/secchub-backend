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
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    /**
     * Duplica solo las clases seleccionadas al semestre actual.
     * Busca las clases por IDs en el semestre origen y las copia al semestre actual, incluyendo horarios y docentes asociados.
     * @param sourceSemesterId ID del semestre origen
     * @param classIds IDs de las clases a duplicar
     * @return cantidad de clases copiadas
     */
    @Transactional
    public int applySelectedClassesToCurrentSemester(Long sourceSemesterId, List<Long> classIds) {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        int count = 0;
        if (sourceSemesterId == null || classIds == null || classIds.isEmpty()) return 0;
        List<Class> classesToCopy = classRepository.findBySemesterId(sourceSemesterId).stream()
                .filter(c -> classIds.contains(c.getId()))
                .toList();
        for (Class original : classesToCopy) {
            // Copiar clase base
            Class nuevaClase = Class.builder()
                    .section(original.getSection())
                    .courseId(original.getCourseId())
                    .semesterId(currentSemesterId)
                    .startDate(original.getStartDate())
                    .endDate(original.getEndDate())
                    .observation(original.getObservation())
                    .capacity(original.getCapacity())
                    .statusId(original.getStatusId())
                    .build();
            Class saved = classRepository.save(nuevaClase);
            // Copiar horarios
            List<ClassSchedule> schedules = classScheduleRepository.findByClassId(original.getId());
            for (ClassSchedule sch : schedules) {
                ClassSchedule nuevoHorario = ClassSchedule.builder()
                        .classId(saved.getId())
                        .classroomId(sch.getClassroomId())
                        .day(sch.getDay())
                        .startTime(sch.getStartTime())
                        .endTime(sch.getEndTime())
                        .modalityId(sch.getModalityId())
                        .disability(sch.getDisability())
                        .build();
                classScheduleRepository.save(nuevoHorario);
            }
            // Copiar docentes asociados
            List<co.edu.puj.secchub_backend.integration.model.TeacherClass> teacherClasses = teacherClassRepository.findByClassId(original.getId());
            for (co.edu.puj.secchub_backend.integration.model.TeacherClass tc : teacherClasses) {
                co.edu.puj.secchub_backend.integration.model.TeacherClass nuevoTc = co.edu.puj.secchub_backend.integration.model.TeacherClass.builder()
                        .semesterId(currentSemesterId)
                        .teacherId(tc.getTeacherId())
                        .classId(saved.getId())
                        .workHours(tc.getWorkHours())
                        .fullTimeExtraHours(tc.getFullTimeExtraHours())
                        .adjunctExtraHours(tc.getAdjunctExtraHours())
                        .decision(tc.getDecision())
                        .observation(tc.getObservation())
                        .statusId(tc.getStatusId())
                        .build();
                teacherClassRepository.save(nuevoTc);
            }
            count++;
        }
        return count;
    }

    /**
     * Devuelve las clases de la sección del usuario y semestre actual que tienen al menos un horario presencial (modality_id=1) sin salón asignado (classroom_id NULL).
     */
    public List<ClassResponseDTO> findClassesWithoutAssignedRoomForSectionChief(Long userId) {
        System.out.println("[DEBUG] userId recibido: " + userId);
        var sectionOpt = sectionRepository.findByUserId(userId);
        if (sectionOpt.isEmpty()) {
            System.out.println("[DEBUG] No se encontró sección para userId: " + userId);
            return List.of();
        }
        Long sectionId = sectionOpt.get().getId();
        System.out.println("[DEBUG] sectionId encontrado: " + sectionId);
        Long semesterId = semesterService.getCurrentSemesterId();
        System.out.println("[DEBUG] semesterId actual: " + semesterId);
        
        // CORRECTED: Filter by course.section_id instead of class.section
        String sql = """
            SELECT DISTINCT c.id
            FROM class c
            INNER JOIN course co ON c.course_id = co.id
            WHERE c.semester_id = ?
              AND co.section_id = ?
            """;
        
        List<Map<String, Object>> classResults = jdbcTemplate.queryForList(sql, semesterId, sectionId);
        List<Long> classIds = classResults.stream()
            .map(row -> ((Number) row.get("id")).longValue())
            .toList();
        
        System.out.println("[DEBUG] Clases encontradas para sección y semestre: " + classIds.size());
        
        List<ClassResponseDTO> result = classIds.stream()
            .map(classId -> classRepository.findById(classId).orElse(null))
            .filter(c -> c != null)
            .filter(c -> {
                List<ClassSchedule> schedules = classScheduleRepository.findByClassId(c.getId());
                // Al menos un horario presencial (modality_id=1) sin classroom_id
                boolean hasMissingRoom = schedules.stream()
                    .anyMatch(sch -> sch.getModalityId() != null && sch.getModalityId() == 1 && sch.getClassroomId() == null);
                if (hasMissingRoom) {
                    System.out.println("[DEBUG] Clase id=" + c.getId() + " SIN salón: al menos un horario presencial sin classroom_id");
                }
                return hasMissingRoom;
            })
            .map(this::mapToResponseDTO)
            .toList();
        System.out.println("[DEBUG] Clases sin salón encontradas: " + result.size());
        for (ClassResponseDTO dto : result) {
            System.out.println("[DEBUG] Clase sin salón: id=" + dto.getId() + ", section=" + dto.getSection() + ", semester=" + dto.getSemesterId());
        }
        return result;
    }
    // Eliminado: getUserIdByEmail. Ahora el controlador obtiene el userId directamente.
    /**
     * Returns classes for the authenticated section chief and current semester that do not have a teacher assigned.
     * @param userId The user ID of the section chief
     * @return List of ClassResponseDTO for classes without assigned teachers
     */
    public List<ClassResponseDTO> findClassesWithoutAssignedTeacherForSectionChief(Long userId) {
            System.out.println("[DEBUG] userId recibido: " + userId);
        // Find the section for the user
        var sectionOpt = sectionRepository.findByUserId(userId);
        if (sectionOpt.isEmpty()) {
                System.out.println("[DEBUG] No se encontró sección para userId: " + userId);
            return List.of();
        }
        Long sectionId = sectionOpt.get().getId();
            System.out.println("[DEBUG] sectionId encontrado: " + sectionId);
        // Get current semester
        Long semesterId = semesterService.getCurrentSemesterId();
            System.out.println("[DEBUG] semesterId actual: " + semesterId);
        
        // CORRECTED: Filter by course.section_id instead of class.section
        String sql = """
            SELECT DISTINCT c.id
            FROM class c
            INNER JOIN course co ON c.course_id = co.id
            WHERE c.semester_id = ?
              AND co.section_id = ?
            """;
        
        List<Map<String, Object>> classResults = jdbcTemplate.queryForList(sql, semesterId, sectionId);
        List<Long> classIds = classResults.stream()
            .map(row -> ((Number) row.get("id")).longValue())
            .toList();
        
        System.out.println("[DEBUG] Clases encontradas para sección y semestre: " + classIds.size());
        
        List<Class> classes = classIds.stream()
            .map(classId -> classRepository.findById(classId).orElse(null))
            .filter(c -> c != null)
            .toList();
        
        for (Class c : classes) {
            System.out.println("[DEBUG] Clase: id=" + c.getId() + ", section=" + c.getSection() + ", semester=" + c.getSemesterId());
        }
        // Only consider classes for this section and semester
        // Filtrar: solo las clases que NO tienen docente asignado (sin registro o con teacher_id NULL)
        List<ClassResponseDTO> result = classes.stream()
            .filter(c -> {
                var teacherClassList = teacherClassRepository.findBySemesterId(semesterId).stream()
                    .filter(tc -> c.getId().equals(tc.getClassId()))
                    .toList();
                if (teacherClassList.isEmpty()) {
                    System.out.println("[DEBUG] Clase id=" + c.getId() + " SIN docente: NO hay registros en teacher_class");
                    return true;
                }
                if (teacherClassList.stream().allMatch(tc -> tc.getTeacherId() == null)) {
                    System.out.println("[DEBUG] Clase id=" + c.getId() + " SIN docente: TODOS los registros tienen teacher_id=NULL");
                    return true;
                }
                return false;
            })
            .map(this::mapToResponseDTO)
            .toList();
        System.out.println("[DEBUG] Clases sin docente encontradas: " + result.size());
        for (ClassResponseDTO dto : result) {
            System.out.println("[DEBUG] Clase sin docente: id=" + dto.getId() + ", section=" + dto.getSection() + ", semester=" + dto.getSemesterId());
        }
        return result;
    }
    private final ModelMapper modelMapper;
    private final ClassRepository classRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AdminModuleSemesterContract semesterService;
    private final AdminModuleCourseContract courseService;
    private final co.edu.puj.secchub_backend.admin.repository.SectionRepository sectionRepository;
    private final co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository teacherClassRepository;
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

    /**
     * Detects schedule conflicts for teachers and classrooms in the current semester
     * for the section of the authenticated section chief.
     * @param sectionId The section ID of the authenticated user
     * @return List of schedule conflicts (teachers and classrooms)
     */
    public List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectScheduleConflicts(Long sectionId) {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Check if there's a current semester
        String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
        List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
        
        if (semesterResult.isEmpty()) {
            return conflicts; // No current semester, return empty list
        }
        
        Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
        
        // 1. Detect teacher conflicts
        conflicts.addAll(detectTeacherConflicts(sectionId, currentSemesterId));
        
        // 2. Detect classroom conflicts
        conflicts.addAll(detectClassroomConflicts(sectionId, currentSemesterId));
        
        return conflicts;
    }

    /**
     * ADMIN METHODS - Global view without section filtering
     */

    /**
     * Get all classes without assigned classroom (global - all sections)
     */
    @Transactional(readOnly = true)
    public List<ClassResponseDTO> getClassesWithoutRoomGlobal() {
        // Get current semester
        String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
        List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
        
        if (semesterResult.isEmpty()) {
            return List.of();
        }
        
        Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
        
        // Get all classes without classroom in current semester (all sections)
        // A class is without room if it has at least one in-person schedule (modality_id = 1) without a classroom
        String sql = """
            SELECT DISTINCT c.id, c.course_id, c.semester_id, c.section, c.capacity, 
                   c.start_date, c.end_date, c.observation, c.status_id, 
                   co.name as course_name, s.name as section_name
            FROM class c
            LEFT JOIN course co ON c.course_id = co.id
            LEFT JOIN section s ON co.section_id = s.id
            WHERE c.semester_id = ?
              AND EXISTS (
                SELECT 1 FROM class_schedule cs 
                WHERE cs.class_id = c.id 
                  AND cs.modality_id = 1
                  AND cs.classroom_id IS NULL
              )
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, currentSemesterId);
        
        return results.stream()
            .map(row -> {
                // Manually build DTO to avoid lazy loading issues
                ClassResponseDTO dto = new ClassResponseDTO();
                dto.setId(((Number) row.get("id")).longValue());
                dto.setCourseId(row.get("course_id") != null ? ((Number) row.get("course_id")).longValue() : null);
                dto.setCourseName((String) row.get("course_name"));
                dto.setSemesterId(row.get("semester_id") != null ? ((Number) row.get("semester_id")).longValue() : null);
                dto.setSection(row.get("section") != null ? ((Number) row.get("section")).longValue() : null);
                dto.setSectionName((String) row.get("section_name"));
                dto.setCapacity(row.get("capacity") != null ? ((Number) row.get("capacity")).intValue() : null);
                dto.setStartDate(row.get("start_date") != null ? ((java.sql.Date) row.get("start_date")).toLocalDate() : null);
                dto.setEndDate(row.get("end_date") != null ? ((java.sql.Date) row.get("end_date")).toLocalDate() : null);
                dto.setObservation((String) row.get("observation"));
                dto.setStatusId(row.get("status_id") != null ? ((Number) row.get("status_id")).longValue() : null);
                dto.setSchedules(new java.util.ArrayList<>()); // Avoid lazy loading
                
                return dto;
            })
            .filter(dto -> dto != null)
            .toList();
    }

    /**
     * Get all classes without assigned teacher (global - all sections)
     */
    @Transactional(readOnly = true)
    public List<ClassResponseDTO> getClassesWithoutTeacherGlobal() {
        // Get current semester
        String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
        List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
        
        if (semesterResult.isEmpty()) {
            return List.of();
        }
        
        Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
        
        // Get all classes without teacher in current semester (all sections)
        String sql = """
            SELECT DISTINCT c.id, c.course_id, c.semester_id, c.section, c.capacity, 
                   c.start_date, c.end_date, c.observation, c.status_id, 
                   co.name as course_name, s.name as section_name
            FROM class c
            LEFT JOIN teacher_class tc ON c.id = tc.class_id
            LEFT JOIN course co ON c.course_id = co.id
            LEFT JOIN section s ON co.section_id = s.id
            WHERE c.semester_id = ?
              AND tc.teacher_id IS NULL
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, currentSemesterId);
        
        return results.stream()
            .map(row -> {
                // Manually build DTO to avoid lazy loading issues
                ClassResponseDTO dto = new ClassResponseDTO();
                dto.setId(((Number) row.get("id")).longValue());
                dto.setCourseId(row.get("course_id") != null ? ((Number) row.get("course_id")).longValue() : null);
                dto.setCourseName((String) row.get("course_name"));
                dto.setSemesterId(row.get("semester_id") != null ? ((Number) row.get("semester_id")).longValue() : null);
                dto.setSection(row.get("section") != null ? ((Number) row.get("section")).longValue() : null);
                dto.setSectionName((String) row.get("section_name"));
                dto.setCapacity(row.get("capacity") != null ? ((Number) row.get("capacity")).intValue() : null);
                dto.setStartDate(row.get("start_date") != null ? ((java.sql.Date) row.get("start_date")).toLocalDate() : null);
                dto.setEndDate(row.get("end_date") != null ? ((java.sql.Date) row.get("end_date")).toLocalDate() : null);
                dto.setObservation((String) row.get("observation"));
                dto.setStatusId(row.get("status_id") != null ? ((Number) row.get("status_id")).longValue() : null);
                dto.setSchedules(new java.util.ArrayList<>()); // Avoid lazy loading
                
                return dto;
            })
            .filter(dto -> dto != null)
            .toList();
    }

    /**
     * Get all pending teacher confirmations (global - all sections)
     */
    public List<Map<String, Object>> getPendingConfirmationsGlobal() {
        // Get current semester
        String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
        List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
        
        if (semesterResult.isEmpty()) {
            return List.of();
        }
        
        Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
        
        // Get all pending confirmations in current semester (all sections)
        // status_id = 4 means pending confirmation
        String sql = """
            SELECT 
                tc.id as teacherClassId,
                tc.teacher_id as teacherId,
                u.name as teacherName,
                tc.class_id as classId,
                co.name as className,
                c.section,
                s.name as sectionName,
                tc.work_hours as workHours,
                tc.observation,
                co.id as courseId,
                co.name as courseName
            FROM teacher_class tc
            INNER JOIN teacher t ON tc.teacher_id = t.id
            INNER JOIN users u ON t.user_id = u.id
            INNER JOIN class c ON tc.class_id = c.id
            INNER JOIN course co ON c.course_id = co.id
            LEFT JOIN section s ON co.section_id = s.id
            WHERE c.semester_id = ?
              AND tc.status_id = 4
            ORDER BY u.name, co.name
            """;
        
        return jdbcTemplate.queryForList(sql, currentSemesterId);
    }

    /**
     * Detect all schedule conflicts (global - all sections)
     */
    public List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectScheduleConflictsGlobal() {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Check if there's a current semester
        String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
        List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
        
        if (semesterResult.isEmpty()) {
            return conflicts; // No current semester, return empty list
        }
        
        Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
        
        // 1. Detect teacher conflicts (global)
        conflicts.addAll(detectTeacherConflictsGlobal(currentSemesterId));
        
        // 2. Detect classroom conflicts (global)
        conflicts.addAll(detectClassroomConflictsGlobal(currentSemesterId));
        
        return conflicts;
    }

    /**
     * Detects teacher conflicts globally (all sections)
     */
    private List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectTeacherConflictsGlobal(Long semesterId) {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Query to get all teacher schedules for the current semester (ALL sections)
        // NOTE: No modality filter - teachers cannot have overlapping classes regardless of modality
        String sql = """
            SELECT 
                tc.teacher_id,
                u.name as teacher_name,
                cs.day,
                cs.start_time,
                cs.end_time,
                c.id as class_id,
                c.section,
                s.name as section_name,
                co.name as class_name
            FROM teacher_class tc
            INNER JOIN teacher t ON tc.teacher_id = t.id
            INNER JOIN users u ON t.user_id = u.id
            INNER JOIN class c ON tc.class_id = c.id
            INNER JOIN course co ON c.course_id = co.id
            LEFT JOIN section s ON co.section_id = s.id
            INNER JOIN class_schedule cs ON c.id = cs.class_id
            WHERE 
                c.semester_id = ?
                AND tc.decision = 1
            ORDER BY tc.teacher_id, cs.day, cs.start_time
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, semesterId);
        
        // Group by teacher_id and day
        Map<String, Map<String, List<Map<String, Object>>>> groupedByTeacherAndDay = new java.util.HashMap<>();
        
        for (Map<String, Object> row : results) {
            Long teacherId = ((Number) row.get("teacher_id")).longValue();
            String day = (String) row.get("day");
            
            groupedByTeacherAndDay
                .computeIfAbsent(teacherId.toString(), k -> new java.util.HashMap<>())
                .computeIfAbsent(day, k -> new java.util.ArrayList<>())
                .add(row);
        }
        
        // Check for overlaps within each teacher-day group
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> teacherEntry : groupedByTeacherAndDay.entrySet()) {
            Long teacherId = Long.valueOf(teacherEntry.getKey());
            
            for (Map.Entry<String, List<Map<String, Object>>> dayEntry : teacherEntry.getValue().entrySet()) {
                List<Map<String, Object>> classes = dayEntry.getValue();
                
                if (classes.size() < 2) continue; // No conflict possible with less than 2 classes
                
                // Check all pairs for overlaps
                List<Map<String, Object>> conflictingClasses = new java.util.ArrayList<>();
                
                for (int i = 0; i < classes.size(); i++) {
                    for (int j = i + 1; j < classes.size(); j++) {
                        Map<String, Object> class1 = classes.get(i);
                        Map<String, Object> class2 = classes.get(j);
                        
                        LocalTime start1 = ((java.sql.Time) class1.get("start_time")).toLocalTime();
                        LocalTime end1 = ((java.sql.Time) class1.get("end_time")).toLocalTime();
                        LocalTime start2 = ((java.sql.Time) class2.get("start_time")).toLocalTime();
                        LocalTime end2 = ((java.sql.Time) class2.get("end_time")).toLocalTime();
                        
                        if (hasOverlap(start1, end1, start2, end2)) {
                            if (!conflictingClasses.contains(class1)) {
                                conflictingClasses.add(class1);
                            }
                            if (!conflictingClasses.contains(class2)) {
                                conflictingClasses.add(class2);
                            }
                        }
                    }
                }
                
                // If we found conflicts, create a ScheduleConflictDTO (no section filtering)
                if (!conflictingClasses.isEmpty()) {
                    String teacherName = (String) conflictingClasses.get(0).get("teacher_name");
                    
                    List<co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO> conflictingClassDTOs = conflictingClasses.stream()
                        .map(c -> co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO.builder()
                            .classId(((Number) c.get("class_id")).longValue())
                            .className((String) c.get("class_name"))
                            .section(((Number) c.get("section")).longValue())
                            .sectionName((String) c.get("section_name"))
                            .day((String) c.get("day"))
                            .startTime(((java.sql.Time) c.get("start_time")).toLocalTime())
                            .endTime(((java.sql.Time) c.get("end_time")).toLocalTime())
                            .build())
                        .toList();
                    
                    conflicts.add(co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO.builder()
                        .type("teacher")
                        .resourceId(teacherId)
                        .resourceName(teacherName)
                        .conflictingClasses(conflictingClassDTOs)
                        .build());
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Detects classroom conflicts globally (all sections)
     */
    private List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectClassroomConflictsGlobal(Long semesterId) {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Query to get all classroom schedules for the current semester (ALL sections)
        String sql = """
            SELECT 
                cs.classroom_id,
                CONCAT(cr.location, ' - ', cr.room) as classroom_name,
                cs.day,
                cs.start_time,
                cs.end_time,
                c.id as class_id,
                c.section,
                s.name as section_name,
                co.name as class_name
            FROM class_schedule cs
            INNER JOIN classroom cr ON cs.classroom_id = cr.id
            INNER JOIN class c ON cs.class_id = c.id
            INNER JOIN course co ON c.course_id = co.id
            LEFT JOIN section s ON co.section_id = s.id
            WHERE 
                c.semester_id = ?
                AND cs.modality_id = 1
            ORDER BY cs.classroom_id, cs.day, cs.start_time
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, semesterId);
        
        // Group by classroom_id and day
        Map<String, Map<String, List<Map<String, Object>>>> groupedByClassroomAndDay = new java.util.HashMap<>();
        
        for (Map<String, Object> row : results) {
            Long classroomId = ((Number) row.get("classroom_id")).longValue();
            String day = (String) row.get("day");
            
            groupedByClassroomAndDay
                .computeIfAbsent(classroomId.toString(), k -> new java.util.HashMap<>())
                .computeIfAbsent(day, k -> new java.util.ArrayList<>())
                .add(row);
        }
        
        // Check for overlaps within each classroom-day group
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> classroomEntry : groupedByClassroomAndDay.entrySet()) {
            Long classroomId = Long.valueOf(classroomEntry.getKey());
            
            for (Map.Entry<String, List<Map<String, Object>>> dayEntry : classroomEntry.getValue().entrySet()) {
                List<Map<String, Object>> classes = dayEntry.getValue();
                
                if (classes.size() < 2) continue; // No conflict possible with less than 2 classes
                
                // Check all pairs for overlaps
                List<Map<String, Object>> conflictingClasses = new java.util.ArrayList<>();
                
                for (int i = 0; i < classes.size(); i++) {
                    for (int j = i + 1; j < classes.size(); j++) {
                        Map<String, Object> class1 = classes.get(i);
                        Map<String, Object> class2 = classes.get(j);
                        
                        LocalTime start1 = ((java.sql.Time) class1.get("start_time")).toLocalTime();
                        LocalTime end1 = ((java.sql.Time) class1.get("end_time")).toLocalTime();
                        LocalTime start2 = ((java.sql.Time) class2.get("start_time")).toLocalTime();
                        LocalTime end2 = ((java.sql.Time) class2.get("end_time")).toLocalTime();
                        
                        if (hasOverlap(start1, end1, start2, end2)) {
                            if (!conflictingClasses.contains(class1)) {
                                conflictingClasses.add(class1);
                            }
                            if (!conflictingClasses.contains(class2)) {
                                conflictingClasses.add(class2);
                            }
                        }
                    }
                }
                
                // If we found conflicts, create a ScheduleConflictDTO (no section filtering)
                if (!conflictingClasses.isEmpty()) {
                    String classroomName = (String) conflictingClasses.get(0).get("classroom_name");
                    
                    List<co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO> conflictingClassDTOs = conflictingClasses.stream()
                        .map(c -> co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO.builder()
                            .classId(((Number) c.get("class_id")).longValue())
                            .className((String) c.get("class_name"))
                            .section(((Number) c.get("section")).longValue())
                            .sectionName((String) c.get("section_name"))
                            .day((String) c.get("day"))
                            .startTime(((java.sql.Time) c.get("start_time")).toLocalTime())
                            .endTime(((java.sql.Time) c.get("end_time")).toLocalTime())
                            .build())
                        .toList();
                    
                    conflicts.add(co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO.builder()
                        .type("classroom")
                        .resourceId(classroomId)
                        .resourceName(classroomName)
                        .conflictingClasses(conflictingClassDTOs)
                        .build());
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Detects conflicts for teachers (same teacher, multiple classes at the same time).
     */
    private List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectTeacherConflicts(Long sectionId, Long semesterId) {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Query to get all teacher schedules for the current semester (ALL sections, to detect global conflicts)
        // NOTE: No modality filter - teachers cannot have overlapping classes regardless of modality
        // CORRECTED: Include course.section_id for proper filtering
        String sql = """
            SELECT 
                tc.teacher_id,
                u.name as teacher_name,
                cs.day,
                cs.start_time,
                cs.end_time,
                c.id as class_id,
                c.section,
                co.section_id,
                co.name as class_name
            FROM teacher_class tc
            INNER JOIN teacher t ON tc.teacher_id = t.id
            INNER JOIN users u ON t.user_id = u.id
            INNER JOIN class c ON tc.class_id = c.id
            INNER JOIN course co ON c.course_id = co.id
            INNER JOIN class_schedule cs ON c.id = cs.class_id
            WHERE 
                c.semester_id = ?
                AND tc.decision = 1
            ORDER BY tc.teacher_id, cs.day, cs.start_time
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, semesterId);
        
        // Group by teacher_id and day
        Map<String, Map<String, List<Map<String, Object>>>> groupedByTeacherAndDay = new java.util.HashMap<>();
        
        for (Map<String, Object> row : results) {
            Long teacherId = ((Number) row.get("teacher_id")).longValue();
            String day = (String) row.get("day");
            
            groupedByTeacherAndDay
                .computeIfAbsent(teacherId.toString(), k -> new java.util.HashMap<>())
                .computeIfAbsent(day, k -> new java.util.ArrayList<>())
                .add(row);
        }
        
        // Check for overlaps within each teacher-day group
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> teacherEntry : groupedByTeacherAndDay.entrySet()) {
            Long teacherId = Long.valueOf(teacherEntry.getKey());
            
            for (Map.Entry<String, List<Map<String, Object>>> dayEntry : teacherEntry.getValue().entrySet()) {
                List<Map<String, Object>> classes = dayEntry.getValue();
                
                if (classes.size() < 2) continue; // No conflict possible with less than 2 classes
                
                // Check all pairs for overlaps
                List<Map<String, Object>> conflictingClasses = new java.util.ArrayList<>();
                
                for (int i = 0; i < classes.size(); i++) {
                    for (int j = i + 1; j < classes.size(); j++) {
                        Map<String, Object> class1 = classes.get(i);
                        Map<String, Object> class2 = classes.get(j);
                        
                        LocalTime start1 = ((java.sql.Time) class1.get("start_time")).toLocalTime();
                        LocalTime end1 = ((java.sql.Time) class1.get("end_time")).toLocalTime();
                        LocalTime start2 = ((java.sql.Time) class2.get("start_time")).toLocalTime();
                        LocalTime end2 = ((java.sql.Time) class2.get("end_time")).toLocalTime();
                        
                        if (hasOverlap(start1, end1, start2, end2)) {
                            // Add both classes to the conflict if not already added
                            if (!conflictingClasses.contains(class1)) {
                                conflictingClasses.add(class1);
                            }
                            if (!conflictingClasses.contains(class2)) {
                                conflictingClasses.add(class2);
                            }
                        }
                    }
                }
                
                // If we found conflicts, check if at least one class belongs to the user's section
                if (!conflictingClasses.isEmpty()) {
                    // CORRECTED: Check if any of the conflicting classes belongs to the current section
                    // Compare with course.section_id instead of class.section
                    boolean affectsCurrentSection = conflictingClasses.stream()
                        .anyMatch(c -> {
                            Object sectionIdObj = c.get("section_id");
                            return sectionIdObj != null && ((Number) sectionIdObj).longValue() == sectionId;
                        });
                    
                    // Only report the conflict if it affects the current section
                    if (affectsCurrentSection) {
                        String teacherName = (String) conflictingClasses.get(0).get("teacher_name");
                        
                        List<co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO> conflictingClassDTOs = conflictingClasses.stream()
                            .map(c -> co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO.builder()
                                .classId(((Number) c.get("class_id")).longValue())
                                .className((String) c.get("class_name"))
                                .section(((Number) c.get("section")).longValue())
                                .day((String) c.get("day"))
                                .startTime(((java.sql.Time) c.get("start_time")).toLocalTime())
                                .endTime(((java.sql.Time) c.get("end_time")).toLocalTime())
                                .build())
                            .toList();
                        
                        conflicts.add(co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO.builder()
                            .type("teacher")
                            .resourceId(teacherId)
                            .resourceName(teacherName)
                            .conflictingClasses(conflictingClassDTOs)
                            .build());
                    }
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Detects conflicts for classrooms (same classroom, multiple classes at the same time).
     */
    private List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> detectClassroomConflicts(Long sectionId, Long semesterId) {
        List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = new java.util.ArrayList<>();
        
        // Query to get all classroom schedules for the current semester (ALL sections, to detect global conflicts)
        // CORRECTED: Include course.section_id for proper filtering
        String sql = """
            SELECT 
                cs.classroom_id,
                CONCAT(cr.location, ' - ', cr.room) as classroom_name,
                cs.day,
                cs.start_time,
                cs.end_time,
                c.id as class_id,
                c.section,
                co.section_id,
                co.name as class_name
            FROM class_schedule cs
            INNER JOIN classroom cr ON cs.classroom_id = cr.id
            INNER JOIN class c ON cs.class_id = c.id
            INNER JOIN course co ON c.course_id = co.id
            WHERE 
                c.semester_id = ?
                AND cs.modality_id = 1
            ORDER BY cs.classroom_id, cs.day, cs.start_time
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, semesterId);
        
        // Group by classroom_id and day
        Map<String, Map<String, List<Map<String, Object>>>> groupedByClassroomAndDay = new java.util.HashMap<>();
        
        for (Map<String, Object> row : results) {
            Long classroomId = ((Number) row.get("classroom_id")).longValue();
            String day = (String) row.get("day");
            
            groupedByClassroomAndDay
                .computeIfAbsent(classroomId.toString(), k -> new java.util.HashMap<>())
                .computeIfAbsent(day, k -> new java.util.ArrayList<>())
                .add(row);
        }
        
        // Check for overlaps within each classroom-day group
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> classroomEntry : groupedByClassroomAndDay.entrySet()) {
            Long classroomId = Long.valueOf(classroomEntry.getKey());
            
            for (Map.Entry<String, List<Map<String, Object>>> dayEntry : classroomEntry.getValue().entrySet()) {
                List<Map<String, Object>> classes = dayEntry.getValue();
                
                if (classes.size() < 2) continue; // No conflict possible with less than 2 classes
                
                // Check all pairs for overlaps
                List<Map<String, Object>> conflictingClasses = new java.util.ArrayList<>();
                
                for (int i = 0; i < classes.size(); i++) {
                    for (int j = i + 1; j < classes.size(); j++) {
                        Map<String, Object> class1 = classes.get(i);
                        Map<String, Object> class2 = classes.get(j);
                        
                        LocalTime start1 = ((java.sql.Time) class1.get("start_time")).toLocalTime();
                        LocalTime end1 = ((java.sql.Time) class1.get("end_time")).toLocalTime();
                        LocalTime start2 = ((java.sql.Time) class2.get("start_time")).toLocalTime();
                        LocalTime end2 = ((java.sql.Time) class2.get("end_time")).toLocalTime();
                        
                        if (hasOverlap(start1, end1, start2, end2)) {
                            // Add both classes to the conflict if not already added
                            if (!conflictingClasses.contains(class1)) {
                                conflictingClasses.add(class1);
                            }
                            if (!conflictingClasses.contains(class2)) {
                                conflictingClasses.add(class2);
                            }
                        }
                    }
                }
                
                // If we found conflicts, check if at least one class belongs to the user's section
                if (!conflictingClasses.isEmpty()) {
                    // CORRECTED: Check if any of the conflicting classes belongs to the current section
                    // Compare with course.section_id instead of class.section
                    boolean affectsCurrentSection = conflictingClasses.stream()
                        .anyMatch(c -> {
                            Object sectionIdObj = c.get("section_id");
                            return sectionIdObj != null && ((Number) sectionIdObj).longValue() == sectionId;
                        });
                    
                    // Only report the conflict if it affects the current section
                    if (affectsCurrentSection) {
                        String classroomName = (String) conflictingClasses.get(0).get("classroom_name");
                        
                        List<co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO> conflictingClassDTOs = conflictingClasses.stream()
                            .map(c -> co.edu.puj.secchub_backend.planning.dto.ConflictingClassDTO.builder()
                                .classId(((Number) c.get("class_id")).longValue())
                                .className((String) c.get("class_name"))
                                .section(((Number) c.get("section")).longValue())
                                .day((String) c.get("day"))
                                .startTime(((java.sql.Time) c.get("start_time")).toLocalTime())
                                .endTime(((java.sql.Time) c.get("end_time")).toLocalTime())
                                .build())
                            .toList();
                        
                        conflicts.add(co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO.builder()
                            .type("classroom")
                            .resourceId(classroomId)
                            .resourceName(classroomName)
                            .conflictingClasses(conflictingClassDTOs)
                            .build());
                    }
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Checks if two time ranges overlap.
     * @param start1 Start time of first range
     * @param end1 End time of first range
     * @param start2 Start time of second range
     * @param end2 End time of second range
     * @return true if there is an overlap (excluding exact consecutive times)
     */
    private boolean hasOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Two ranges overlap if: start1 < end2 AND start2 < end1
        // This excludes exactly consecutive times (e.g., 10:00-12:00 and 12:00-14:00)
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
