package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleTeacherContract;
import co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassAssignHoursRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassAssignHoursResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassNotFoundException;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassServerErrorException;
import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import co.edu.puj.secchub_backend.planning.contract.PlanningModuleClassContract;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service handling business logic for HU17 (Professor availability).
 * A teacher can see pending classes and accept/reject them.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherClassService {
    private final SecurityModuleUserContract userService;
    private final AdminModuleSectionContract sectionService;
    private final AdminModuleSemesterContract semesterService;
    private final PlanningModuleClassContract classService;
    private final AdminModuleTeacherContract teacherService;

    private final TeacherClassRepository repository;
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_ACCEPTED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    /**
     * Creates a new teacher-class assignment.
     * @param TeacherClassRequestDTO with assignment data
     * @return TeacherClassResponseDTO with created assignment
     */
    public Mono<TeacherClassResponseDTO> createTeacherClass(TeacherClassRequestDTO request) {
        return semesterService.getCurrentSemesterId()
            .flatMap(currentSemesterId -> {
                TeacherClass teacherClass = modelMapper.map(request, TeacherClass.class);
                teacherClass.setStatusId(STATUS_PENDING_ID);
                teacherClass.setSemesterId(currentSemesterId);
                return repository.save(teacherClass);
            })
            .map(saved -> modelMapper.map(saved, TeacherClassResponseDTO.class))
            .onErrorMap(error -> {
                log.error("Error creating TeacherClass: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to create TeacherClass");
            });
    }

    /**
     * Lists all teacher classes for the current semester.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @return Flux of TeacherClassResponseDTO for the current semester
     */
    public Flux<TeacherClassResponseDTO> listCurrentSemesterTeacherClasses() {
        return semesterService.getCurrentSemesterId()
            .flatMapMany(currentSemesterId ->
                repository.findBySemesterId(currentSemesterId)
                    .filterWhen(this::filterTeacherClass)
                    .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            )
            .onErrorMap(error -> {
                log.error("Error listing current semester teacher classes: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list current semester teacher classes");
            });
    }

    /**
     * Lists all teacher classes for a specific teacher in the current semester.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @param teacherId Teacher ID
     * @return Flux of TeacherClassResponseDTO for the teacher in the current semester
     */
    public Flux<TeacherClassResponseDTO> listCurrentSemesterTeacherClassesByTeacher(Long teacherId) {
        return semesterService.getCurrentSemesterId()
            .flatMapMany(currentSemesterId ->
                repository.findBySemesterIdAndTeacherId(currentSemesterId, teacherId)
                    .filterWhen(this::filterTeacherClass)
                    .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            )
            .onErrorMap(error -> {
                log.error("Error listing current semester teacher classes by teacher: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list current semester teacher classes by teacher");
            });
    }

    /**
     * Lists all classes (accepted, pending, rejected) assigned to a teacher.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @param teacherId teacher id
     * @return Flux of TeacherClassResponseDTO
     */
    public Flux<TeacherClassResponseDTO> listAllTeacherClassByTeacher(Long teacherId) {
        return repository.findByTeacherId(teacherId)
            .filterWhen(this::filterTeacherClass)
            .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            .onErrorMap(error -> {
                log.error("Error listing all teacher classes by teacher: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list all teacher classes by teacher");
            });
    }

    /**
     * Lists all classes pending decision for the current semester.
     * @return Flux of TeacherClassResponseDTO with pending decision
     */
    public Flux<TeacherClassResponseDTO> listPendingDecisionClassesForCurrentSemester() {
        return semesterService.getCurrentSemesterId()
            .flatMapMany(currentSemesterId ->
                repository.findBySemesterIdAndStatusId(currentSemesterId, STATUS_PENDING_ID)
                    .filterWhen(this::filterTeacherClass)
                    .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            )
            .onErrorMap(error -> {
                log.error("Error listing pending decision classes for current semester: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list pending decision classes for current semester");
            });
    }

    /**
     * Lists only classes filtered by status.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @param teacherId teacher id
     * @param statusId status (4=pending, 8=accepted, 9=rejected)
     * @return Flux of TeacherClassResponseDTO
     */
    public Flux<TeacherClassResponseDTO> listTeacherClassByStatus(Long teacherId, Long statusId) {
        return repository.findByTeacherIdAndStatusId(teacherId, statusId)
            .filterWhen(this::filterTeacherClass)
            .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            .onErrorMap(error -> {
                log.error("Error listing teacher classes by status: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list teacher classes by status");
            });
    }

    /**
     * Lists all classes for a given class ID.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @param classId class ID
     * @return Flux of TeacherClassResponseDTO for the given class ID
     */
    public Flux<TeacherClassResponseDTO> listTeacherClassByClassId(Long classId) {
        return repository.findByClassId(classId)
            .filterWhen(this::filterTeacherClass)
            .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            .onErrorMap(error -> {
                log.error("Error listing teacher classes by class ID: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to list teacher classes by class ID");
            });
    }

    /**
     * Accepts a class (set decision true and status=ACCEPTED).
     * If the current user has ROLE_SECTION, only teacher classes for their section can be accepted.
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    public Mono<TeacherClassResponseDTO> acceptTeacherClass(Long teacherClassId, String observation) {
        return repository.findById(teacherClassId)
            .filterWhen(this::filterTeacherClass)
            .switchIfEmpty(Mono.error(new TeacherClassNotFoundException(
                "TeacherClass not found for acceptance with id: " + teacherClassId)))
            .flatMap(teacherClass -> {
                teacherClass.setDecision(true);
                teacherClass.setStatusId(STATUS_ACCEPTED_ID);
                teacherClass.setObservation(observation);
                return repository.save(teacherClass);
            })
            .map(saved -> modelMapper.map(saved, TeacherClassResponseDTO.class))
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                if (error instanceof TeacherClassNotFoundException) {
                    return error;
                }
                log.error("Error accepting teacher class: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to accept teacher class");
            });
    }

    /**
     * Rejects a class (set decision false and status=REJECTED).
     * If the current user has ROLE_SECTION, only teacher classes for their section can be rejected.
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    public Mono<TeacherClassResponseDTO> rejectTeacherClass(Long teacherClassId, String observation) {
        return repository.findById(teacherClassId)
            .filterWhen(this::filterTeacherClass)
            .switchIfEmpty(Mono.error(new TeacherClassNotFoundException(
                "TeacherClass not found for rejection with id: " + teacherClassId)))
            .flatMap(teacherClass -> {
                teacherClass.setDecision(false);
                teacherClass.setStatusId(STATUS_REJECTED_ID);
                teacherClass.setObservation(observation);
                return repository.save(teacherClass);
            })
            .map(saved -> modelMapper.map(saved, TeacherClassResponseDTO.class))
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                if (error instanceof TeacherClassNotFoundException) {
                    return error;
                }
                log.error("Error rejecting teacher class: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to reject teacher class");
            });
    }

    /**
     * Deletes a teacher-class assignment by teacher ID and class ID.
     * If the current user has ROLE_SECTION, only teacher classes for their section can be deleted.
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return Mono<Void>
     */
    public Mono<Void> deleteTeacherClassByTeacherAndClass(Long teacherId, Long classId) {
        return repository.findByTeacherIdAndClassId(teacherId, classId)
            .filterWhen(this::filterTeacherClass)
            .switchIfEmpty(Mono.error(new TeacherClassNotFoundException(
                "TeacherClass not found for deletion with teacherId: " + teacherId + " and classId: " + classId)))
            .flatMap(teacherClass -> repository.deleteById(teacherClass.getId()))
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                if (error instanceof TeacherClassNotFoundException) {
                    return error;
                }
                log.error("Error deleting teacher class: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to delete teacher class");
            });
    }

    /**
     * Gets a teacher-class assignment by teacher ID and class ID.
     * If the current user has ROLE_SECTION, only teacher classes for their section are returned.
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return TeacherClassResponseDTO
     */
    public Mono<TeacherClassResponseDTO> getTeacherClassByTeacherAndClass(Long teacherId, Long classId) {
        return repository.findByTeacherIdAndClassId(teacherId, classId)
            .filterWhen(this::filterTeacherClass)
            .switchIfEmpty(Mono.error(new TeacherClassNotFoundException(
                "TeacherClass not found with teacherId: " + teacherId + " and classId: " + classId)))
            .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
            .onErrorMap(error -> {
                if (error instanceof TeacherClassNotFoundException) {
                    return error;
                }
                log.error("Error retrieving teacher class by teacher and class: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to retrieve teacher class by teacher and class");
            });
    }

    /**
     * Updates a teacher-class assignment dates
     * @param teacherClassId Teacher-Class assignment ID
     * @param startDate New start date
     * @param endDate New end date
     * @return Updated teacher-class assignment
     */
    public Mono<TeacherClassResponseDTO> updateTeachingDates(Long teacherClassId, LocalDate startDate, LocalDate endDate) {
        return repository.findById(teacherClassId)
            .filterWhen(this::filterTeacherClass)
            .switchIfEmpty(Mono.error(new TeacherClassNotFoundException(
                "TeacherClass not found for updating dates with id: " + teacherClassId)))
            .flatMap(teacherClass -> {
                teacherClass.setStartDate(startDate);
                teacherClass.setEndDate(endDate);
                return repository.save(teacherClass);
            })
            .map(saved -> modelMapper.map(saved, TeacherClassResponseDTO.class))
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                if (error instanceof TeacherClassNotFoundException) {
                    return error;
                }
                log.error("Error updating teaching dates: {}", error.getMessage());
                throw new TeacherClassServerErrorException("Failed to update teaching dates");
            });
    }

    /**
     * Gets a teacher-class extra hours warning when assigning work to a teacher.
     * @param teacherId Teacher ID
     * @param teacherClassAssignHoursRequestDTO Request body containing hours to assign
     * @return Mono<TeacherClassAssignHoursResponseDTO> containing the warning details
     */
    public Mono<TeacherClassAssignHoursResponseDTO> getTeacherExtraHoursWarning(Long teacherId, TeacherClassAssignHoursRequestDTO teacherClassAssignHoursRequestDTO) {
        return semesterService.getCurrentSemesterId()
            .flatMap(currentSemesterId ->
                Mono.zip(
                    teacherService.getTeacherById(teacherId),
                    // Get by teacher id and semester id
                    repository.findBySemesterIdAndTeacherId(currentSemesterId, teacherId)
                        .collectList()
                        .flatMap(teacherClasses -> {
                            int currentAssignedHours = teacherClasses.stream()
                                .mapToInt(TeacherClass::getWorkHours)
                                .sum();
                            return Mono.just(currentAssignedHours);
                        })
                )
            )
            .flatMap(tuple -> {
                TeacherResponseDTO teacher = tuple.getT1();
                Integer currentAssignedHours = tuple.getT2();
                int workHoursToAssign = teacherClassAssignHoursRequestDTO.getWorkHoursToAssign() != null ? teacherClassAssignHoursRequestDTO.getWorkHoursToAssign() : 0;

                int total = currentAssignedHours + workHoursToAssign;
                int excessHours = Math.max(total - teacher.getMaxHours(), 0);

                return userService.getUserInformationById(teacher.getUserId())
                    .flatMap(userInfo ->
                        Mono.just(
                            TeacherClassAssignHoursResponseDTO.builder()
                            .teacherName(userInfo.getName())
                            .maxHours(teacher.getMaxHours())
                            .totalAssignedHours(currentAssignedHours)
                            .workHoursToAssign(workHoursToAssign)
                            .exceedsMaxHours(excessHours)
                            .build()
                        )
                    );
            })
            .onErrorMap(
                error -> {
                    log.error("Error getting teacher extra hours warning: {}", error.getMessage());
                    return new TeacherClassServerErrorException("Failed to get teacher extra hours warning");
                }
            );
    }

    // =====================================================
    // Private methods
    // =====================================================

    /**
     * Filters teacher classes by user role and if they are role users by their section.
     * @param teacherClass instance of TeacherClass
     * @return true mono boolean indicating if the teacherClass passes the filter or not
     */
    private Mono<Boolean> filterTeacherClass(TeacherClass teacherClass) {
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

                // Check if user has TEACHER role
                boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER"));

                if (isTeacher) {
                    return userService.getUserIdByEmail(userEmail)
                        .flatMap(teacherService::getTeacherIdByUserId)
                        .flatMap(teacherId -> Mono.just(teacherId.equals(teacherClass.getTeacherId())));
                }

                return userService.getUserIdByEmail(userEmail)
                    .flatMap(sectionService::getSectionIdByUserId)
                    .flatMap(sectionId -> classService.isClassInSection(teacherClass.getClassId(), sectionId))
                    .defaultIfEmpty(false);
            })
            .defaultIfEmpty(false);
    }
}
