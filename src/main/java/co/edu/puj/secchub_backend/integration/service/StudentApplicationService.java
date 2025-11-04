package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.contract.IntegrationModuleStudentApplicationContract;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class StudentApplicationService implements IntegrationModuleStudentApplicationContract {
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    private final StudentApplicationRepository studentApplicationRepository;
    private final StudentApplicationScheduleRepository requestScheduleRepository;

    private final SecurityModuleUserContract userService;

    private final AdminModuleSemesterContract semesterService;
    private final AdminModuleSectionContract sectionService;
    private final AdminModuleCourseContract courseService;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_APPROVED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    /**
     * Creates a new student application with its associated schedules.
     * @param studentApplicationRequestDTO with application data
     * @return Created application
     */
    public Mono<StudentApplicationResponseDTO> createStudentApplication(StudentApplicationRequestDTO studentApplicationRequestDTO) {
        return semesterService.getCurrentSemesterId()
            .flatMap(semesterId -> {
                StudentApplication studentApplication = modelMapper.map(studentApplicationRequestDTO, StudentApplication.class);
                studentApplication.setSemesterId(semesterId);
                studentApplication.setStatusId(STATUS_PENDING_ID);
                studentApplication.setApplicationDate(LocalDate.now());

                return getLoggedInUserId(studentApplication, semesterId)
                    .switchIfEmpty(Mono.error(new StudentApplicationBadRequestException("User already has a student application for this semester with the same section or course.")))
                    .flatMap(userId -> {
                        studentApplication.setUserId(userId);
                        return studentApplicationRepository.save(studentApplication);
                    });
            })
            .flatMap(savedApplication -> {
                List<StudentApplicationScheduleRequestDTO> scheduleDTOs = studentApplicationRequestDTO.getSchedules();
                if (scheduleDTOs == null || scheduleDTOs.isEmpty()) {
                    return Mono.just(savedApplication);
                }

                List<StudentApplicationSchedule> schedules = scheduleDTOs.stream()
                    .map(dto -> {
                        StudentApplicationSchedule schedule = new StudentApplicationSchedule();
                        schedule.setStudentApplicationId(savedApplication.getId());
                        schedule.setDay(dto.getDay());
                        schedule.setStartTime(parseTimeString(dto.getStartTime()));
                        schedule.setEndTime(parseTimeString(dto.getEndTime()));
                        return schedule;
                    })
                    .toList();

                return requestScheduleRepository.saveAll(schedules)
                    .collectList()
                    .thenReturn(savedApplication);
            })
            .flatMap(this::getStudentApplicationWithSchedules)
            .as(transactionalOperator::transactional);
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
     * If the current user has ROLE_SECTION, only applications for their section are returned.
     * @return Flux of student applications for the current semester
     */
    public Flux<StudentApplicationResponseDTO> listCurrentSemesterStudentApplications() {
        return semesterService.getCurrentSemesterId()
            .flatMapMany(currentSemesterId ->
                studentApplicationRepository.findBySemesterId(currentSemesterId)
                    .filterWhen(this::filterByUserRole)
                    .flatMap(this::getStudentApplicationWithSchedules)
            );
    }

    /**
     * Obtains all student applications.
     * If the current user has ROLE_SECTION, only applications for their section are returned.
     * @return Flux of all student applications
     */
    public Flux<StudentApplicationResponseDTO> listAllStudentApplications() {
        return studentApplicationRepository.findAll()
            .filterWhen(this::filterByUserRole)
            .flatMap(this::getStudentApplicationWithSchedules);
    }

    /**
     * Finds a student application by its ID.
     * If the current user has ROLE_SECTION, only the application for their section is returned.
     * @param studentApplicationId Application ID
     * @return StudentApplicationResponseDTO with the given ID
     */
    public Mono<StudentApplicationResponseDTO> findStudentApplicationById(Long studentApplicationId) {
        return studentApplicationRepository.findById(studentApplicationId)
            .filterWhen(this::filterByUserRole)
            .switchIfEmpty(Mono.error(new StudentApplicationNotFoundException("StudentApplication not found for consult: " + studentApplicationId)))
            .flatMap(this::getStudentApplicationWithSchedules);
    }

    /**
     * Lists student applications by their status ID.
     * If the current user has ROLE_SECTION, only applications for their section are returned.
     * @param statusId Status ID
     * @return Flux of student applications with the given status ID
     */
    public Flux<StudentApplicationResponseDTO> listStudentApplicationsByStatus(Long statusId) {
        return studentApplicationRepository.findByStatusId(statusId)
            .filterWhen(this::filterByUserRole)
            .flatMap(this::getStudentApplicationWithSchedules);
    }

    /**
     * Lists student applications for a specific section.
     * If the current user has ROLE_SECTION, only applications for their section are returned.
     * @param sectionId Section ID
     * @return Flux of student applications for the given section
     */
    public Flux<StudentApplicationResponseDTO> listStudentApplicationsForSection(Long sectionId) {
        return studentApplicationRepository.findRequestsForSection(sectionId)
            .filterWhen(this::filterByUserRole)
            .flatMap(this::getStudentApplicationWithSchedules);
    }

    /**
     * Approves a student application by setting its status to the approved status ID.
     * If the current user has ROLE_SECTION, only the application for their section can be approved.
     * @param studentApplicationId Student Application ID
     * @return empty Mono when done
     */
    public Mono<Void> approveStudentApplication(Long studentApplicationId) {
        return studentApplicationRepository.findById(studentApplicationId)
            .filterWhen(this::filterByUserRole)
            .switchIfEmpty(Mono.error(new StudentApplicationNotFoundException("StudentApplication not found for approval: " + studentApplicationId)))
            .flatMap(student -> {
                student.setStatusId(STATUS_APPROVED_ID);
                return studentApplicationRepository.save(student);
            })
            .then();
    }

    /**
     * Rejects a student application by setting its status to the rejected status ID.
     * If the current user has ROLE_SECTION, only the application for their section can be rejected.
     * @param studentApplicationId Student Application ID
     * @return empty Mono when done
     */
    public Mono<Void> rejectStudentApplication(Long studentApplicationId) {
        return studentApplicationRepository.findById(studentApplicationId)
            .filterWhen(this::filterByUserRole)
            .switchIfEmpty(Mono.error(new StudentApplicationNotFoundException("StudentApplication not found for rejection: " + studentApplicationId)))
            .flatMap(student -> {
                student.setStatusId(STATUS_REJECTED_ID);
                return studentApplicationRepository.save(student);
            })
            .then();
    }

    /**
     * Helper method to get student application with schedules (reactive).
     * @param studentApplication StudentApplication entity
     * @return Mono<StudentApplicationResponseDTO> with schedules
     */
    private Mono<StudentApplicationResponseDTO> getStudentApplicationWithSchedules(StudentApplication studentApplication) {
        return requestScheduleRepository.findByStudentApplicationId(studentApplication.getId())
            .map(schedule -> StudentApplicationScheduleResponseDTO.builder()
                .id(schedule.getId())
                .studentApplicationId(schedule.getStudentApplicationId())
                .day(schedule.getDay())
                .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                .build())
            .collectList()
            .map(scheduleDTOs -> {
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

                responseDTO.setSchedules(scheduleDTOs);

                // Format application date as string
                if (studentApplication.getApplicationDate() != null) {
                    responseDTO.setApplicationDate(studentApplication.getApplicationDate().toString());
                }

                return responseDTO;
            });
    }

    // ==================================
    // Security filter methods
    // ==================================

    /**
     * Implements the method to check if a student application belongs to a specific section.
     * @param applicationId The ID of the student application.
     * @param sectionId The ID of the section.
     * @return true if the application belongs to the section, false otherwise.
     */
    @Override
    public Mono<Boolean> isApplicationOfSection(Long applicationId, Long sectionId) {
        return studentApplicationRepository.findById(applicationId)
                .flatMap(application -> {
                    if (application.getSectionId() != null) {
                        return Mono.just(application.getSectionId().equals(sectionId));
                    }

                    if (application.getCourseId() != null){
                        return courseService.getCourseSectionId(application.getCourseId())
                                .map(secId -> secId.equals(sectionId));
                    }

                    return Mono.just(false);
                })
                .switchIfEmpty(Mono.just(false));
    }

    /**
     * Searches for the logged-in user's ID.
     * If the user has already a student application for the given semester, with same section/course, it returns empty.
     * Otherwise, it returns the user ID.
     * @param application instance of student application to check
     * @param semesterId semester ID
     * @return Mono<Long> with the user ID or empty if application already exists
     */
    private Mono<Long> getLoggedInUserId(StudentApplication application, Long semesterId) {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication().getName())
            .flatMap(userService::getUserIdByEmail)
            .flatMap(userId ->
                studentApplicationRepository
                    .findByUserIdAndSemesterId(userId, semesterId)
                    .filter(existingApplication -> {
                        boolean sameSection = (application.getSectionId() != null && application.getSectionId().equals(existingApplication.getSectionId()));
                        boolean sameCourse = (application.getCourseId() != null && application.getCourseId().equals(existingApplication.getCourseId()));
                        return sameSection || sameCourse;
                    })
                    .hasElement()
                    .flatMap(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            return Mono.empty();
                        } else {
                            return Mono.just(userId);
                        }
                    })
            )
            .onErrorMap(error -> {
                log.error("Error obtaining logged-in user ID: {}", error.getMessage());
                throw new StudentApplicationBadRequestException("Could not obtain logged-in user ID: " + error.getMessage());
            });
    }

    /**
     * Filters student applications by logged-in user's role.
     * @param application instance of student applications to filter
     * @return Mono<Boolean> indicating if the user has access to the application
     */
    private Mono<Boolean> filterByUserRole(StudentApplication application) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Authentication authentication = securityContext.getAuthentication();
                
                // Check if user has ADMIN role
                boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                if (isAdmin) {
                    // Admin can see all classes
                    return Mono.just(true);
                }

                // For ROLE_SECTION users, filter by their section
                String userEmail = authentication.getName();

                return userService.getUserIdByEmail(userEmail)
                    .flatMap(sectionService::getSectionIdByUserId)
                    .flatMap(userSection -> this.isApplicationOfSection(application.getId(), userSection));
            })
            .defaultIfEmpty(false);
    }
}
