package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.*;
import co.edu.puj.secchub_backend.integration.exception.StudentApplicationBadRequestException;
import co.edu.puj.secchub_backend.integration.exception.StudentApplicationNotFoundException;
import co.edu.puj.secchub_backend.integration.exception.TimeParsingException;
import co.edu.puj.secchub_backend.integration.model.*;
import co.edu.puj.secchub_backend.integration.repository.*;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentApplicationService {
    private final ModelMapper modelMapper;
    private final StudentApplicationRepository studentRepo;
    private final StudentApplicationScheduleRepository requestScheduleRepository;
    private final SecurityModuleUserContract userService;
    private final AdminModuleSemesterContract semesterService;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_APPROVED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    /**
     * Creates a new student application with its associated schedules.
     * @param studentApplicationRequestDTO with application data
     * @return Created application
     */
    @Transactional
    public Mono<StudentApplicationResponseDTO> createStudentApplication(StudentApplicationRequestDTO studentApplicationRequestDTO) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(identifier -> Mono.fromCallable(() -> {
                    Long userId = userService.getUserIdByEmail(identifier);
                    StudentApplication student = modelMapper.map(studentApplicationRequestDTO, StudentApplication.class);
                    Long currentSemesterId = semesterService.getCurrentSemesterId();

                    student.setUserId(userId);
                    student.setSemesterId(currentSemesterId);
                    student.setApplicationDate(LocalDate.now());
                    student.setStatusId(STATUS_PENDING_ID);

                    if (studentApplicationRequestDTO.getSchedules() == null || studentApplicationRequestDTO.getSchedules().isEmpty()) {
                        throw new StudentApplicationBadRequestException("Schedules are required for the student application.");
                    }

                    // Create schedules and establish bidirectional relationship
                    List<StudentApplicationSchedule> schedules = new java.util.ArrayList<>();
                    for (StudentApplicationScheduleRequestDTO scheduleDTO : studentApplicationRequestDTO.getSchedules()) {
                        StudentApplicationSchedule studentSchedule = new StudentApplicationSchedule();
                        studentSchedule.setDay(scheduleDTO.getDay());
                        studentSchedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                        studentSchedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                        studentSchedule.setStudentApplication(student);
                        schedules.add(studentSchedule);
                    }
                    student.setSchedules(schedules);

                    // Save student application with cascading schedules
                    StudentApplication saved = studentRepo.save(student);

                    return mapToResponseDTO(saved);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Helper method to parse time string (HH:mm:ss) to java.sql.Time
     * @param timeString Time in format "HH:mm:ss"
     * @return java.sql.Time object or null if timeString is null/empty
     * @throws TimeParsingException if the time string format is invalid
     */
    private Time parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            // Parse the time string and convert to java.sql.Time
            LocalTime localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
            return Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            throw new TimeParsingException("Invalid time format: '" + timeString + "'. Expected format: HH:mm:ss", e);
        }
    }

    /**
     * Obtains all student applications for the current semester.
     * @return List of student applications for the current semester
     */
    public List<StudentApplicationResponseDTO> listCurrentSemesterStudentApplications() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return studentRepo.findBySemesterId(currentSemesterId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Obtains all student applications.
     * @return List of student applications
     */
    public List<StudentApplicationResponseDTO> listAllStudentApplications() {
        return studentRepo.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Finds a student application by its ID.
     * @param studentApplicationId Application ID
     * @return StudentApplicationResponseDTO with the given ID
     */
    public Mono<StudentApplicationResponseDTO> findStudentApplicationById(Long studentApplicationId) {
        return Mono.fromCallable(() -> {
            StudentApplication studentApplication = studentRepo.findById(studentApplicationId)
                    .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for consult: " + studentApplicationId));
            return mapToResponseDTO(studentApplication);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists student applications by their status ID.
     * @param statusId Status ID
     * @return List of student applications with the given status ID
     */
    public List<StudentApplicationResponseDTO> listStudentApplicationsByStatus(Long statusId) {
        return studentRepo.findByStatusId(statusId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Lists student applications for a specific section.
     * @param sectionId Section ID
     * @return List of student applications for the given section
     */
    public List<StudentApplicationResponseDTO> listStudentApplicationsForSection(Long sectionId) {
        return studentRepo.findRequestsForSection(sectionId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Approves a student application by setting its status to the approved status ID.
     * @param studentApplicationId Student Application ID
     * @return empty Mono when done
     */
    public Mono<Void> approveStudentApplication(Long studentApplicationId) {
        return Mono.fromCallable(() -> {
            StudentApplication student = studentRepo.findById(studentApplicationId)
                    .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for approval: " + studentApplicationId));
            student.setStatusId(STATUS_APPROVED_ID);
            studentRepo.save(student);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Rejects a student application by setting its status to the rejected status ID.
     * @param studentApplicationId Student Application ID
     * @return empty Mono when done
     */
    public Mono<Void> rejectStudentApplication(Long studentApplicationId) {
        return Mono.fromCallable(() -> {
            StudentApplication student = studentRepo.findById(studentApplicationId)
                    .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for rejection: " + studentApplicationId));
            student.setStatusId(STATUS_REJECTED_ID);
            studentRepo.save(student);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Helper method to convert StudentApplication entity to ResponseDTO.
     * @param studentApplication StudentApplication entity
     * @return StudentApplicationResponseDTO
     */
    private StudentApplicationResponseDTO mapToResponseDTO(StudentApplication studentApplication) {
        // Manual mapping to avoid ModelMapper issues with Hibernate lazy collections
        StudentApplicationResponseDTO responseDTO = StudentApplicationResponseDTO.builder()
                .id(studentApplication.getId())
                .userId(studentApplication.getUserId())
                .courseId(studentApplication.getCourseId())
                .sectionId(studentApplication.getSectionId())
                .semesterId(studentApplication.getSemesterId())
                .program(studentApplication.getProgram())
                .studentSemester(studentApplication.getStudentSemester())
                .academicAverage(studentApplication.getAcademicAverage())
                .phoneNumber(studentApplication.getPhoneNumber())
                .alternatePhoneNumber(studentApplication.getAlternatePhoneNumber())
                .address(studentApplication.getAddress())
                .personalEmail(studentApplication.getPersonalEmail())
                .wasTeachingAssistant(studentApplication.getWasTeachingAssistant())
                .courseAverage(studentApplication.getCourseAverage())
                .courseTeacher(studentApplication.getCourseTeacher())
                .statusId(studentApplication.getStatusId())
                .build();
        
        // Map schedules
        List<StudentApplicationSchedule> schedules = requestScheduleRepository.findByStudentApplicationId(studentApplication.getId());
        List<StudentApplicationScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> StudentApplicationScheduleResponseDTO.builder()
                        .id(schedule.getId())
                        .studentApplicationId(schedule.getStudentApplicationId())
                        .day(schedule.getDay())
                        .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                        .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                        .build())
                .toList();
        
        responseDTO.setSchedules(scheduleDTOs);
        
        // Format application date as string
        if (studentApplication.getApplicationDate() != null) {
            responseDTO.setApplicationDate(studentApplication.getApplicationDate().toString());
        }
        
        return responseDTO;
    }
}
