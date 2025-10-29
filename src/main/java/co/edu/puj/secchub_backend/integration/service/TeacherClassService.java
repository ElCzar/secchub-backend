package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassNotFoundException;
import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Service handling business logic for HU17 (Professor availability).
 * A teacher can see pending classes and accept/reject them.
 */
@Service
@RequiredArgsConstructor
public class TeacherClassService {
    // TODO: Add notifications when an admin/user wants to inform a teacher of a new class assignment

    private final TeacherClassRepository repository;
    private final ModelMapper modelMapper;
    private final AdminModuleSemesterContract semesterService;
    private final JdbcTemplate jdbcTemplate;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_IN_PROGRESS_ID = 6L;
    private static final Long STATUS_ACCEPTED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    /**
     * Creates a new teacher-class assignment.
     * @param TeacherClassRequestDTO with assignment data
     * @return TeacherClassResponseDTO with created assignment
     */
    public Mono<TeacherClassResponseDTO> createTeacherClass(co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO request) {
        return Mono.fromCallable(() -> {
            TeacherClass teacherClass = modelMapper.map(request, TeacherClass.class);
            teacherClass.setStatusId(STATUS_PENDING_ID);
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            teacherClass.setSemesterId(currentSemesterId);
            TeacherClass saved = repository.save(teacherClass);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists all teacher classes for the current semester.
     * @return List of TeacherClassResponseDTO for the current semester
     */
    public List<TeacherClassResponseDTO> listCurrentSemesterTeacherClasses() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return repository.findBySemesterId(currentSemesterId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists all teacher classes for a specific teacher in the current semester.
     * @param teacherId Teacher IDfindBySemesterIdAndTeacherId
     * @return List of TeacherClassResponseDTO for the teacher in the current semester
     */
    public List<TeacherClassResponseDTO> listCurrentSemesterTeacherClassesByTeacher(Long teacherId) {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return repository.findBySemesterIdAndTeacherId(currentSemesterId, teacherId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists all classes (accepted, pending, rejected) assigned to a teacher.
     * @param teacherId teacher id
     * @return List of TeacherClassResponseDTO
     */
    public List<TeacherClassResponseDTO> listAllTeacherClassByTeacher(Long teacherId) {
        return repository.findByTeacherId(teacherId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists only classes filtered by status.
     * @param teacherId teacher id
     * @param statusId status (1=pending, 2=accepted, 3=rejected)
     * @return List of TeacherClassResponseDTO
     */
    public List<TeacherClassResponseDTO> listTeacherClassByStatus(Long teacherId, Long statusId) {
        return repository.findByTeacherIdAndStatusId(teacherId, statusId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists all classes for a given class ID with enriched teacher information.
     * @param classId
     * @return List of TeacherClassResponseDTO for the given class ID with teacher details
     */
    public List<TeacherClassResponseDTO> listTeacherClassByClassId(Long classId) {
        String sql = """
            SELECT tc.id, tc.semester_id, tc.teacher_id, tc.class_id, tc.work_hours,
                   tc.full_time_extra_hours, tc.adjunct_extra_hours, tc.decision, 
                   tc.observation, tc.status_id, tc.start_date, tc.end_date,
                   u.name as teacher_name, u.last_name as teacher_last_name, 
                   u.email as teacher_email, t.max_hours as teacher_max_hours
            FROM teacher_class tc
            JOIN teacher t ON tc.teacher_id = t.id
            JOIN users u ON t.user_id = u.id
            WHERE tc.class_id = ?
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            TeacherClassResponseDTO.builder()
                .id(rs.getLong("id"))
                .semesterId(rs.getLong("semester_id"))
                .teacherId(rs.getLong("teacher_id"))
                .classId(rs.getLong("class_id"))
                .workHours(rs.getInt("work_hours"))
                .fullTimeExtraHours((Integer) rs.getObject("full_time_extra_hours"))
                .adjunctExtraHours((Integer) rs.getObject("adjunct_extra_hours"))
                .decision((Boolean) rs.getObject("decision"))
                .observation(rs.getString("observation"))
                .statusId(rs.getLong("status_id"))
                .startDate(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null)
                .endDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null)
                .teacherName(rs.getString("teacher_name"))
                .teacherLastName(rs.getString("teacher_last_name"))
                .teacherEmail(rs.getString("teacher_email"))
                .teacherMaxHours((Integer) rs.getObject("teacher_max_hours"))
                .teacherContractType("N/A") // Por ahora hardcodeado, se puede mejorar después
                .build(), classId
        );
    }

    /**
     * Accepts a class (set decision true and status=2).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    @Transactional
    public Mono<TeacherClassResponseDTO> acceptTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for acceptance: " + teacherClassId));
            tc.setDecision(true);
            tc.setStatusId(STATUS_ACCEPTED_ID);
            tc.setObservation(observation);
            TeacherClass saved = repository.save(tc);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Rejects a class (set decision false and status=3).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    @Transactional
    public Mono<TeacherClassResponseDTO> rejectTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for rejection: " + teacherClassId));
            tc.setDecision(false);
            tc.setStatusId(STATUS_REJECTED_ID);
            tc.setObservation(observation);
            TeacherClass saved = repository.save(tc);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates the teaching dates (start and end date) for a teacher-class assignment.
     * @param teacherClassId relation id
     * @param startDate new start date for the teacher
     * @param endDate new end date for the teacher
     * @return updated TeacherClassResponseDTO
     */
    @Transactional
    public Mono<TeacherClassResponseDTO> updateTeachingDates(Long teacherClassId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for date update: " + teacherClassId));
            tc.setStartDate(startDate);
            tc.setEndDate(endDate);
            TeacherClass saved = repository.save(tc);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a teacher-class assignment by teacher ID and class ID.
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return Mono<TeacherClassResponseDTO>
     */
    public Mono<TeacherClassResponseDTO> getTeacherClassByTeacherAndClass(Long teacherId, Long classId) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findByTeacherIdAndClassId(teacherId, classId)
                    .orElseThrow(() -> new TeacherClassNotFoundException(
                            "TeacherClass not found for teacherId: " + teacherId + " and classId: " + classId));
            return modelMapper.map(tc, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a teacher-class assignment by teacher ID and class ID.
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteTeacherClassByTeacherAndClass(Long teacherId, Long classId) {
        return Mono.fromRunnable(() -> {
            TeacherClass tc = repository.findByTeacherIdAndClassId(teacherId, classId)
                    .orElseThrow(() -> new TeacherClassNotFoundException(
                            "TeacherClass not found for teacherId: " + teacherId + " and classId: " + classId));
            repository.delete(tc);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}
