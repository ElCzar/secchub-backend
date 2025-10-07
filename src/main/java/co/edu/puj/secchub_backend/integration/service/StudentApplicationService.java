package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.*;
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
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentApplicationService {
    private final ModelMapper modelMapper;
    private final StudentApplicationRepository studentRepo;
    private final StudentApplicationScheduleRepository requestScheduleRepository;
    private final SecurityModuleUserContract userService;

    /**
     * Creates a new student application with its associated schedules.
     * @param studentApplicationDTO with application data
     * @return Created application
     */
    @Transactional
    public Mono<StudentApplication> createStudentApplication(StudentApplicationDTO studentApplicationDTO) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName()) // Get username or email
                .flatMap(identifier -> Mono.fromCallable(() -> {
                    // Use the user service to get user ID by email
                    Long userId = userService.getUserIdByEmail(identifier);
                    
                    // Map DTO to entity
                    StudentApplication student = modelMapper.map(studentApplicationDTO, StudentApplication.class);
                    
                    // Set automatic values
                    student.setUserId(userId); // Set user_id from authenticated user
                    student.setApplicationDate(LocalDate.now()); // Set current date
                    student.setStatusId(1L); // Set status to "Active" (ID: 1)
                    student.setId(null); // Ignore ID from frontend
                    
                    StudentApplication saved = studentRepo.save(student);

                    if (studentApplicationDTO.getSchedules() != null) {
                        for (ScheduleDTO scheduleDTO : studentApplicationDTO.getSchedules()) {
                            // Manual mapping instead of using ModelMapper for time conversion
                            StudentApplicationSchedule studentSchedule = new StudentApplicationSchedule();
                            studentSchedule.setDay(scheduleDTO.getDay());
                            studentSchedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                            studentSchedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                            studentSchedule.setStudentApplicationId(saved.getId());
                            
                            requestScheduleRepository.save(studentSchedule);
                        }
                    }

                    return saved;
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
        } catch (Exception e) {
            throw new TimeParsingException("Invalid time format: '" + timeString + "'. Expected format: HH:mm:ss", e);
        }
    }

    /**
     * Obtains all student applications.
     * @return Stream of student applications
     */
    public Flux<StudentApplication> listAllStudentApplications() {
        return Mono.fromCallable(studentRepo::findAll)
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds a student application by its ID.
     * @param studentApplicationId Application ID
     * @return Student with the given ID
     */
    public Mono<StudentApplication> findStudentApplicationById(Long studentApplicationId) {
        return Mono.fromCallable(() -> studentRepo.findById(studentApplicationId)
                .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for consult: " + studentApplicationId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists student applications by their status ID.
     * @param statusId Status ID
     * @return Stream of student applications with the given status ID
     */
    public List<StudentApplication> listStudentApplicationsByStatus(Long statusId) {
        return Mono.fromCallable(() -> studentRepo.findByStatusId(statusId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic())
                .collectList()
                .block();
    }

    /**
     * Lists student applications for a specific section.
     * @param sectionId Section ID
     * @return Stream of student applications for the given section
     */
    public List<StudentApplication> listStudentApplicationsForSection(Long sectionId) {
        return Mono.fromCallable(() -> studentRepo.findRequestsForSection(sectionId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic())
                .collectList()
                .block();
    }

    /**
     * Approves a student application by setting its status to the approved status ID.
     * @param studentApplicationId Student Application ID
     * @param statusApprovedId Approved Status ID
     * @return empty Mono when done
     */
    public Mono<Void> approveStudentApplication(Long studentApplicationId, Long statusApprovedId) {
        return Mono.fromCallable(() -> {
            StudentApplication student = studentRepo.findById(studentApplicationId)
                    .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for approval: " + studentApplicationId));
            student.setStatusId(statusApprovedId);
            studentRepo.save(student);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Rejects a student application by setting its status to the rejected status ID.
     * @param studentApplicationId Student Application ID
     * @param statusRejectedId Rejected Status ID
     * @return empty Mono when done
     */
    public Mono<Void> rejectStudentApplication(Long studentApplicationId, Long statusRejectedId) {
        return Mono.fromCallable(() -> {
            StudentApplication student = studentRepo.findById(studentApplicationId)
                    .orElseThrow(() -> new StudentApplicationNotFoundException("StudentApplication not found for rejection: " + studentApplicationId));
            student.setStatusId(statusRejectedId);
            studentRepo.save(student);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
