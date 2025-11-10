package co.edu.puj.secchub_backend.planning.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleTeacherContract;
import co.edu.puj.secchub_backend.planning.contract.PlanningModuleClassContract;
import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomScheduleConflictResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassCreationException;
import co.edu.puj.secchub_backend.planning.exception.ClassNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.ClassScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.PlanningBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.PlanningServerErrorException;
import co.edu.puj.secchub_backend.planning.model.Class;
import co.edu.puj.secchub_backend.planning.model.ClassSchedule;
import co.edu.puj.secchub_backend.planning.repository.ClassRepository;
import co.edu.puj.secchub_backend.planning.repository.ClassScheduleRepository;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class for handling planning-related operations.
 * This class manages the core business logic for the planning module.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlanningService implements PlanningModuleClassContract {
    
    private final ModelMapper modelMapper;
    private final ClassRepository classRepository;
    private final ClassScheduleRepository classScheduleRepository;

    private final AdminModuleSemesterContract semesterService;
    private final AdminModuleSectionContract sectionService;
    private final AdminModuleCourseContract courseService;
    private final AdminModuleTeacherContract teacherService;
    private final SecurityModuleUserContract userService;

    private final ClassroomService classroomService;

    private final TransactionalOperator transactionalOperator;

    // ========================================================================
    // Class Methods
    // ========================================================================

    /**
     * Creates a new class with schedules.
     * @param classCreateRequestDTO DTO with class information
     * @return Created class DTO
     */
    public Mono<ClassResponseDTO> createClass(ClassCreateRequestDTO classCreateRequestDTO) {
        return semesterService.getCurrentSemesterId()
            .flatMap(currentSemesterId -> {
                Class classEntity = this.mapToEntity(classCreateRequestDTO);
                classEntity.setSemesterId(currentSemesterId);
                return classRepository.save(classEntity)
                    .flatMap(savedClass -> {
                        ClassResponseDTO responseDTO = this.mapToResponseDTO(savedClass);
                        List<ClassScheduleRequestDTO> schedulesDto = classCreateRequestDTO.getSchedules();

                        if (schedulesDto == null || schedulesDto.isEmpty()) {
                            return Mono.just(responseDTO);
                        }

                        return Flux.fromIterable(schedulesDto)
                            .map(dto -> {
                                ClassSchedule schedule = this.mapToEntity(dto);
                                schedule.setClassId(savedClass.getId());
                                return schedule;
                            })
                            .collectList()
                            .flatMapMany(classScheduleRepository::saveAll)
                            .map(this::mapToResponseDTO)
                            .collectList()
                            .map(savedSchedules -> {
                                responseDTO.setSchedules(savedSchedules);
                                return responseDTO;
                            });
                    });
            })
            .as(transactionalOperator::transactional)
            .onErrorMap(e -> {
                log.error("Error creating class: {}", e.getMessage());
                throw new ClassCreationException("Error creating class: " + e.getMessage());
            });
    }

    /**
     * Gets all classes for the current semester.
     * If the current user has ROLE_SECTION, only classes for their section are returned.
     * @return Flux of classes for the current semester
     */
    public Flux<ClassResponseDTO> findCurrentSemesterClasses() {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            classRepository.findBySemesterId(currentSemesterId)
                .filterWhen(this::filterClassByUserSection)
                .flatMap(this::getClassSchedulesForClass)
        ).onErrorMap(e -> {
            log.error("Error retrieving classes for current semester: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes for current semester: " + e.getMessage());
        });
    }

    /**
     * Gets all classes.
     * If the current user has ROLE_SECTION, only classes for their section are returned.
     * @return Flux of all classes
     */
    public Flux<ClassResponseDTO> findAllClasses() {
        return classRepository.findAll()
        .filterWhen(this::filterClassByUserSection)
        .flatMap(this::getClassSchedulesForClass)
        .onErrorMap(e -> {
            log.error("Error retrieving all classes: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving all classes: " + e.getMessage());
        });
    }

    /**
     * Gets a class by ID.
     * If the current user has ROLE_SECTION, only the class for their section is returned.
     * @param classId Class ID
     * @return Class found
     */
    public Mono<ClassResponseDTO> findClassById(Long classId) {
        return classRepository.findById(classId)
        .filterWhen(this::filterClassByUserSection)
        .flatMap(this::getClassSchedulesForClass)
        .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found with id: " + classId)));
    }

    /**
     * Gets classes by course ID.
     * If the current user has ROLE_SECTION, only classes for their section are returned.
     * @param courseId Course ID
     * @return Flux of classes for the specified course
     */
    public Flux<ClassResponseDTO> findClassesByCourse(Long courseId) {
        return classRepository.findByCourseId(courseId)
        .filterWhen(this::filterClassByUserSection)
        .flatMap(this::getClassSchedulesForClass)
        .onErrorMap(e -> {
            log.error("Error retrieving all classes by course ID {}: {}", courseId, e.getMessage());
            throw new PlanningServerErrorException("Error retrieving all classes: " + e.getMessage());
        });
    }

    /**
     * Gets classes by section.
     * If the current user has ROLE_SECTION, only classes for their section are returned.
     * @param section Section number
     * @return Flux of classes for the specified section
     */
    public Flux<ClassResponseDTO> findClassesBySection(Long section) {
        return classRepository.findBySection(section)
        .filterWhen(this::filterClassByUserSection)
        .flatMap(this::getClassSchedulesForClass)
        .onErrorMap(e -> {
            log.error("Error retrieving classes by section {}: {}", section, e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes by section: " + e.getMessage());
        });
    }

    /**
     * Gets classes by semester and course for the current semester.
     * If the current user has ROLE_SECTION, only classes for their section are returned.
     * @param courseId Course ID
     * @return List of classes for the current semester and specified course
     */
    public Flux<ClassResponseDTO> findCurrentSemesterClassesByCourse(Long courseId) {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            classRepository.findBySemesterIdAndCourseId(currentSemesterId, courseId)
                .filterWhen(this::filterClassByUserSection)
                .flatMap(this::getClassSchedulesForClass)
        ).onErrorMap(e -> {
            log.error("Error retrieving classes for current semester and course ID {}: {}", courseId, e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes for current semester and course: " + e.getMessage());
        });
    }

    /**
     * Find classes by semester id
     * If the current user has ROLE_SECTION, only the classes for their section are returned.
     * @param semesterId Semester ID
     * @return Flux of classes for the semester
     */
    public Flux<ClassResponseDTO> findClassesBySemester(Long semesterId) {
        return classRepository.findBySemesterId(semesterId)
        .filterWhen(this::filterClassByUserSection)
        .flatMap(this::getClassSchedulesForClass)
        .onErrorMap(e -> {
            log.error("Error retrieving classes by semester ID {}: {}", semesterId, e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes by semester: " + e.getMessage());
        });
    }

    /**
     * Updates a class.
     * If the current user has ROLE_SECTION, only the class for their section is updated.
     * @param classId Class ID
     * @param classCreateRequestDTO DTO with updated data
     * @return Updated class
     */
    public Mono<ClassResponseDTO> updateClass(Long classId, ClassCreateRequestDTO classCreateRequestDTO) {
        return classRepository.findById(classId)
        .filterWhen(this::filterClassByUserSection)
        .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for update with id: " + classId)))
        .flatMap(existingClass -> {
            modelMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);
            modelMapper.map(classCreateRequestDTO, existingClass);
            return classRepository.save(existingClass);
        })
        .map(this::mapToResponseDTO)
        .onErrorMap(e -> {
            log.error("Error updating class with id {}: {}", classId, e.getMessage());
            if (e instanceof ClassNotFoundException) {
                return e;
            }
            throw new PlanningServerErrorException("Error updating class: " + e.getMessage());
        });
    }

    /**
     * Deletes a class by ID.
     * If the current user has ROLE_SECTION, only the class for their section is deleted.
     * @param classId Class ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClass(Long classId) {
        return classRepository.findById(classId)
        .filterWhen(this::filterClassByUserSection)
        .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for deletion with id: " + classId)))
        .flatMap(existing -> classRepository.deleteById(existing.getId()));
    }

    // ========================================================================
    // Class Schedule Methods
    // ========================================================================

    /**
     * Adds a schedule to a class.
     * If the current user has ROLE_SECTION, only the class for their section is updated.
     * @param classId Class ID
     * @param classScheduleRequestDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<ClassScheduleResponseDTO> addClassSchedule(Long classId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return classRepository.findById(classId)
        .filterWhen(this::filterClassByUserSection)
        .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for adding schedule with id " + classId)))
        .flatMap(classEntity -> {
            ClassSchedule classSchedule = this.mapToEntity(classScheduleRequestDTO);
            classSchedule.setClassId(classId);
            return classScheduleRepository.save(classSchedule);
        })
        .map(this::mapToResponseDTO);
    }

    /**
     * Gets schedules associated with a class.
     * If the current user has ROLE_SECTION, only schedules for their section are returned.
     * @param classId Class ID
     * @return Flux of schedules
     */
    public Flux<ClassScheduleResponseDTO> findClassSchedulesByClassId(Long classId) {
        return classRepository.findById(classId)
        .filterWhen(this::filterClassByUserSection)
        .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for retrieval with id: " + classId)))
        .flatMapMany(classEntity -> classScheduleRepository.findByClassId(classId)
                .map(this::mapToResponseDTO));
    }

    /**
     * Gets a class schedule by ID.
     * If the current user has ROLE_SECTION, only the schedule for their section is returned.
     * @param scheduleId Schedule ID
     * @return Class schedule found
     */
    public Mono<ClassScheduleResponseDTO> findClassScheduleById(Long scheduleId) {
        return classScheduleRepository.findById(scheduleId)
        .switchIfEmpty(Mono.error(new ClassScheduleNotFoundException("Class schedule not found for retrieval with id: " + scheduleId)))
        .flatMap(classSchedule -> 
            classRepository.findById(classSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for class with id: " + classSchedule.getId() + " and schedule id: " + scheduleId)))
            .thenReturn(classSchedule)
        )
        .map(this::mapToResponseDTO);
    }

    /**
     * Updates a specific class schedule.
     * If the current user has ROLE_SECTION, only the schedule for their section is updated.
     * @param scheduleId Schedule ID
     * @param classScheduleRequestDTO DTO with updated data
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> updateClassSchedule(Long scheduleId, ClassScheduleRequestDTO classScheduleRequestDTO) {
        return classScheduleRepository.findById(scheduleId)
        .switchIfEmpty(Mono.error(new ClassScheduleNotFoundException("Class schedule not found for update with id: " + scheduleId)))
        .flatMap(existingSchedule ->
            classRepository.findById(existingSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .switchIfEmpty(Mono.error(new ClassNotFoundException("Class for update not found for schedule with id: " + scheduleId)))
            .thenReturn(existingSchedule)
        )
        .flatMap(existingSchedule -> {
            modelMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);
            modelMapper.map(classScheduleRequestDTO, existingSchedule);
            return classScheduleRepository.save(existingSchedule);
        })
        .map(this::mapToResponseDTO)
        .onErrorMap(e -> {
            log.error("Error updating class schedule with id {}: {}", scheduleId, e.getMessage());
            if (e instanceof ClassScheduleNotFoundException) {
                return e;
            }

            if (e instanceof ClassNotFoundException) {
                return e;
            }
            throw new PlanningServerErrorException("Error updating class schedule: " + e.getMessage());
        });
    }

    /**
     * Deletes a class schedule by ID.
     * If the current user has ROLE_SECTION, only the schedule for their section is deleted.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteClassSchedule(Long scheduleId) {
        return classScheduleRepository.findById(scheduleId)
        .switchIfEmpty(Mono.error(new ClassScheduleNotFoundException("Class schedule not found for deletion with id: " + scheduleId)))
        .flatMap(existingSchedule ->
            classRepository.findById(existingSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .switchIfEmpty(Mono.error(new ClassNotFoundException("Class not found for schedule deletion with id: " + scheduleId)))
            .thenReturn(existingSchedule.getId())
        )
        .flatMap(classScheduleRepository::deleteById);
    }

    /**
     * Partially updates a class schedule.
     * If the current user has ROLE_SECTION, only the schedule for their section is updated.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Updated schedule
     */
    public Mono<ClassScheduleResponseDTO> patchClassSchedule(Long scheduleId, Map<String, Object> updates) {
        return classScheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new ClassScheduleNotFoundException(
                "Class schedule not found for retrieval with id: " + scheduleId)))
            .flatMap(scheduleFilter -> 
                classRepository.findById(scheduleFilter.getClassId())
                .filterWhen(this::filterClassByUserSection)
                .switchIfEmpty(Mono.error(new ClassNotFoundException(
                    "Class not found for schedule patch with id: " + scheduleId)))
                .thenReturn(scheduleFilter)
            )
            .flatMap(schedule -> {
                // Apply partial updates
                updates.forEach((key, value) -> {
                    switch (key) {
                        case "startTime" -> {
                            if (value instanceof String stringValue)
                                schedule.setStartTime(LocalTime.parse(stringValue));
                            else if (value instanceof LocalTime localTimeValue)
                                schedule.setStartTime(localTimeValue);
                        }
                        case "endTime" -> {
                            if (value instanceof String stringValue)
                                schedule.setEndTime(LocalTime.parse(stringValue));
                            else if (value instanceof LocalTime localTimeValue)
                                schedule.setEndTime(localTimeValue);
                        }
                        case "day" -> schedule.setDay((String) value);
                        case "classroomId" -> schedule.setClassroomId((Long) value);
                        case "modalityId" -> schedule.setModalityId((Long) value);
                        case "disability" -> schedule.setDisability((Boolean) value);
                        default -> throw new PlanningBadRequestException("Invalid field for schedule update: " + key);
                    }
                });

                return classScheduleRepository.save(schedule);
            })
            .map(saved -> modelMapper.map(saved, ClassScheduleResponseDTO.class));
    }

    /**
     * Gets schedules by classroom ID.
     * If the current user has ROLE_SECTION, only the schedules for their section are returned.
     * @param classroomId Classroom ID
     * @return Flux of schedules for the specified classroom
     */
    public Flux<ClassScheduleResponseDTO> findClassSchedulesByClassroom(Long classroomId) {
        return classScheduleRepository.findByClassroomId(classroomId)
        .flatMap(classSchedule ->
            classRepository.findById(classSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .map(classEntity -> classSchedule)
        )
        .map(this::mapToResponseDTO);
    }

    /**
     * Gets schedules by day.
     * If the current user has ROLE_SECTION, only the schedules for their section are returned.
     * @param day Day of the week
     * @return Flux of schedules for the specified day
     */
    public Flux<ClassScheduleResponseDTO> findClassSchedulesByDay(String day) {
        return classScheduleRepository.findByDay(day)
        .flatMap(classSchedule ->
            classRepository.findById(classSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .map(classEntity -> classSchedule)
        )
        .map(this::mapToResponseDTO);
    }

    /**
     * Gets schedules with disability accommodations.
     * If the current user has ROLE_SECTION, only the schedules for their section are returned.
     * @param disability True to find schedules with disability accommodations
     * @return Flux of schedules with disability considerations
     */
    public Flux<ClassScheduleResponseDTO> findClassSchedulesByDisability(Boolean disability) {
        return classScheduleRepository.findByDisability(disability)
        .flatMap(classSchedule ->
            classRepository.findById(classSchedule.getClassId())
            .filterWhen(this::filterClassByUserSection)
            .map(classEntity -> classSchedule)
        )
        .map(this::mapToResponseDTO);
    }

    /**
     * Duplicate planning from source semester to target semester
     * If the current user has ROLE_SECTION, only the classes for their section are returned.
     * @param sourceSemesterId Source semester ID
     * @param targetSemesterId Target semester ID
     * @return Flux of created classes in target semester
     */
    public Flux<ClassResponseDTO> duplicateSemesterPlanning(Long sourceSemesterId, Long targetSemesterId) {
        return semesterService.getSemesterById(targetSemesterId)
        .flatMapMany(targetSemester ->
            classRepository.findBySemesterId(sourceSemesterId)
            .filterWhen(this::filterClassByUserSection)
            .flatMap(src -> {
                Class copy = new Class();
                modelMapper.map(src, copy);
                copy.setId(null);
                copy.setSemesterId(targetSemesterId);
                copy.setStartDate(targetSemester.getStartDate());
                copy.setEndDate(targetSemester.getEndDate());
                return classRepository.save(copy)
                .flatMapMany(saved ->
                    classScheduleRepository.findByClassId(src.getId())
                    .flatMap(schedule -> {
                        ClassSchedule scheduleCopy = new ClassSchedule();
                        modelMapper.map(schedule, scheduleCopy);
                        scheduleCopy.setId(null);
                        scheduleCopy.setClassId(saved.getId());
                        return classScheduleRepository.save(scheduleCopy);
                    })
                    .then(Mono.just(saved))
                );
            })
            .flatMap(this::getClassSchedulesForClass)
        )
        .as(transactionalOperator::transactional)
        .onErrorMap(e -> {
            log.error("Error duplicating semester planning from {} to {}: {}", sourceSemesterId, targetSemesterId, e.getMessage());
            throw new PlanningServerErrorException("Error duplicating semester planning: " + e.getMessage());
        });
    }

    /**
     * Apply planning from source semester to current semester
     * If the current user has ROLE_SECTION, only the classes for their section are returned.
     * @param sourceSemesterId Source semester ID
     * @return Map with operation result
     */
    public Flux<ClassResponseDTO> applySemesterPlanningToCurrent(Long sourceSemesterId) {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            this.duplicateSemesterPlanning(sourceSemesterId, currentSemesterId)
        );
    }

    /**
     * Apply planning from source class ids to current semester
     * @param sourceClassIds List of source class IDs
     * @return Flux of ClassResponseDTO for the duplicated classes
     */
    public Flux<ClassResponseDTO> duplicateClassPlanning(List<Long> sourceClassIds) {
        return semesterService.getCurrentSemester()
        .flatMapMany(currentSemester ->
            Flux.fromIterable(sourceClassIds)
            .flatMap(sourceClassId ->
                classRepository.findById(sourceClassId)
                .filterWhen(this::filterClassByUserSection)
                .switchIfEmpty(Mono.error(new ClassNotFoundException("Source class not found with id: " + sourceClassId)))
                .flatMapMany(src -> {
                    Class copy = new Class();
                    modelMapper.map(src, copy);
                    copy.setId(null);
                    copy.setSemesterId(currentSemester.getId());
                    copy.setStartDate(currentSemester.getStartDate());
                    copy.setEndDate(currentSemester.getEndDate());
                    return classRepository.save(copy)
                    .flatMapMany(saved ->
                        classScheduleRepository.findByClassId(src.getId())
                        .flatMap(schedule -> {
                            ClassSchedule scheduleCopy = new ClassSchedule();
                            modelMapper.map(schedule, scheduleCopy);
                            scheduleCopy.setId(null);
                            scheduleCopy.setClassId(saved.getId());
                            return classScheduleRepository.save(scheduleCopy);
                        })
                        .then(Mono.just(saved))
                    );
                })
                .flatMap(this::getClassSchedulesForClass)
            )
        )
        .as(transactionalOperator::transactional)
        .onErrorMap(e -> {
            log.error("Error duplicating class planning for class IDs {}: {}", sourceClassIds, e.getMessage());

            if (e instanceof ClassNotFoundException) {
                return e;
            }

            throw new PlanningServerErrorException("Error duplicating class planning: " + e.getMessage());
        });
    }
    

    /**
     * Finds all classes without classroom assigned in the current semester.
     * If the current user has ROLE_SECTION, only the classes for their section are returned.
     * @return Flux of classes without classroom assigned
     */
    public Flux<ClassResponseDTO> findClassesWithoutClassroomAssigned() {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            classRepository.findBySemesterIdAndNoClassroomAssigned(currentSemesterId)
                .filterWhen(this::filterClassByUserSection)
                .flatMap(this::getClassSchedulesForClass)
        ).onErrorMap(e -> {
            log.error("Error retrieving classes without classroom assigned for current semester: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes without classroom assigned for current semester: " + e.getMessage());
        });
    }

    /**
     * Finds all classes without teacher assigned in the current semester.
     * If the current user has ROLE_SECTION, only the classes for their section are returned.
     * @return Flux of classes without teacher assigned
     */
    public Flux<ClassResponseDTO> findClassesWithoutTeacherAssigned() {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            classRepository.findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId)
                .filterWhen(this::filterClassByUserSection)
                .flatMap(this::getClassSchedulesForClass)
        ).onErrorMap(e -> {
            log.error("Error retrieving classes without teacher assigned for current semester: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classes without teacher assigned for current semester: " + e.getMessage());
        });
    }

    /**
     * Obtains schedule conflicts for classrooms in the current semester.
     * Groups overlapping schedules by classroom and filters based on user permissions.
     * Creates separate conflict groups for each cluster of overlapping schedules.
     * @return Flux of classroom schedule conflicts
     */
    public Flux<ClassroomScheduleConflictResponseDTO> getClassroomScheduleConflicts() {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            classroomService.getAllClassrooms()
            .flatMap(classroom ->
                classScheduleRepository.findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroom.getId())
                .collectList()
                .filter(schedulesWithConflicts -> !schedulesWithConflicts.isEmpty())
                .flatMapMany(schedulesWithConflicts -> {
                    // Group schedules into clusters based on overlaps
                    List<List<ClassSchedule>> clusters = groupSchedulesIntoOverlapClusters(schedulesWithConflicts);
                    
                    // Create a conflict DTO for each cluster with at least 2 schedules
                    return Flux.fromIterable(clusters)
                    .filter(cluster -> cluster.size() >= 2)
                    // Filter if none of the schedules in the cluster belong to classes accessible by the user
                    .filterWhen(cluster ->
                        Flux.fromIterable(cluster)
                        .flatMap(schedule ->
                            classRepository.findById(schedule.getClassId())
                            .filterWhen(this::filterClassByUserSection)
                        )
                        .hasElements()
                    )
                    .map(cluster -> {
                        ClassroomScheduleConflictResponseDTO conflictDTO = new ClassroomScheduleConflictResponseDTO();
                        conflictDTO.setClassroomId(classroom.getId());
                        conflictDTO.setClassroomName(classroom.getRoom());
                        conflictDTO.setConflictingClassesIds(
                            cluster.stream()
                            .map(ClassSchedule::getClassId)
                            .distinct()
                            .toList()
                        );
                        conflictDTO.setConflictStartTime(
                            cluster.stream()
                            .map(ClassSchedule::getStartTime)
                            .min(LocalTime::compareTo)
                            .orElse(cluster.get(0).getStartTime())
                        );
                        conflictDTO.setConflictEndTime(
                            cluster.stream()
                            .map(ClassSchedule::getEndTime)
                            .max(LocalTime::compareTo)
                            .orElse(cluster.get(0).getEndTime())
                        );
                        conflictDTO.setDay(cluster.get(0).getDay());
                        return conflictDTO;
                    });
                })
            )
        )
        .onErrorMap(e -> {
            log.error("Error retrieving classroom schedule conflicts: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving classroom schedule conflicts: " + e.getMessage());
        });
    }

    /**
     * Obtains schedule conflicts for teachers in the current semester.
     * Groups overlapping schedules by teacher and filters based on user permissions.
     * Creates separate conflict groups for each cluster of overlapping schedules.
     * @return Flux of teacher schedule conflicts
     */
    public Flux<TeacherScheduleConflictResponseDTO> getTeacherScheduleConflicts() {
        return semesterService.getCurrentSemesterId()
        .flatMapMany(currentSemesterId ->
            teacherService.getAllTeachers()
            .flatMap(teacher ->
                classScheduleRepository.findTeacherScheduleConflicts(currentSemesterId, teacher.getId())
                .collectList()
                .filter(schedulesWithConflicts -> !schedulesWithConflicts.isEmpty())
                .flatMapMany(schedulesWithConflicts -> {
                    // Group schedules into clusters based on overlaps
                    List<List<ClassSchedule>> clusters = groupSchedulesIntoOverlapClusters(schedulesWithConflicts);
                    
                    // Create a conflict DTO for each cluster with at least 2 schedules
                    return Flux.fromIterable(clusters)
                    .filter(cluster -> cluster.size() >= 2)
                    // Filter if none of the schedules in the cluster belong to classes accessible by the user
                    .filterWhen(cluster ->
                        Flux.fromIterable(cluster)
                        .flatMap(schedule ->
                            classRepository.findById(schedule.getClassId())
                            .filterWhen(this::filterClassByUserSection)
                        )
                        .hasElements()
                    )
                    .flatMap(cluster ->
                        // Get user information for the teacher
                        userService.getUserInformationById(teacher.getUserId())
                        .map(user -> {
                            TeacherScheduleConflictResponseDTO conflictDTO = new TeacherScheduleConflictResponseDTO();
                            conflictDTO.setUserId(user.getId());
                            conflictDTO.setUserName(user.getName() + " " + user.getLastName());
                            conflictDTO.setConflictingClassesIds(
                                cluster.stream()
                                .map(ClassSchedule::getClassId)
                                .distinct()
                                .toList()
                            );
                            
                            LocalTime minStartTime = cluster.stream()
                                .map(ClassSchedule::getStartTime)
                                .min(LocalTime::compareTo)
                                .orElse(cluster.get(0).getStartTime());
                            
                            LocalTime maxEndTime = cluster.stream()
                                .map(ClassSchedule::getEndTime)
                                .max(LocalTime::compareTo)
                                .orElse(cluster.get(0).getEndTime());
                            
                            conflictDTO.setConflictStartTime(minStartTime.toString());
                            conflictDTO.setConflictEndTime(maxEndTime.toString());
                            conflictDTO.setConflictDay(cluster.get(0).getDay());
                            
                            return conflictDTO;
                        })
                    );
                })
            )
        )
        .onErrorMap(e -> {
            log.error("Error retrieving teacher schedule conflicts: {}", e.getMessage());
            throw new PlanningServerErrorException("Error retrieving teacher schedule conflicts: " + e.getMessage());
        });
    }

    // ========================================================================
    // Private Methods
    // ========================================================================    
    
    /**
     * Groups schedules where ALL schedules in each cluster directly overlap with ALL others.
     * Uses a set to track already-processed schedules to avoid duplicate clusters.
     * A schedule is only added to a cluster if it overlaps with EVERY existing member.
     * @param schedules List of schedules to group
     * @return List of clusters with overlapping schedules
     */
    private List<List<ClassSchedule>> groupSchedulesIntoOverlapClusters(List<ClassSchedule> schedules) {
        List<List<ClassSchedule>> clusters = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (int i = 0; i < schedules.size()*schedules.size(); i++) {
            int index = i % schedules.size();
            List<ClassSchedule> cluster = new ArrayList<>();
            cluster.add(schedules.get(index));

            for (int j = 0; j < schedules.size(); j++) {
                if (
                    j == index || 
                    processed.contains(schedules.get(j).getId() + "-" + schedules.get(index).getId()) ||
                    processed.contains(schedules.get(index).getId() + "-" + schedules.get(j).getId())
                ) {
                    continue;
                }
                
                final int currentIndex = j;
                boolean overlapsWithAll = cluster.stream()
                    .allMatch(s -> schedulesOverlap(s, schedules.get(currentIndex)));
                
                if (overlapsWithAll) {
                    final int finalJ = j;
                    cluster.stream()
                        .forEach(s -> 
                            processed.add(s.getId() + "-" + schedules.get(finalJ).getId())
                        );
                    cluster.add(schedules.get(j));
                }
            }
            
            if (cluster.size() > 1) clusters.add(cluster);
        }
        return clusters;
    }

    
    /**
     * Checks if two schedules overlap in time on the same day.
     * @param schedule1 First schedule
     * @param schedule2 Second schedule
     * @return true if schedules overlap, false otherwise
     */
    private boolean schedulesOverlap(ClassSchedule schedule1, ClassSchedule schedule2) {
        // Must be on the same day
        if (!schedule1.getDay().equals(schedule2.getDay())) {
            return false;
        }
        
        // Check time overlap: (start1 < end2) AND (end1 > start2)
        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) && 
            schedule1.getEndTime().isAfter(schedule2.getStartTime());
    }

    /**
     * Obtains all schedules for a class
     * @param classEntity Class entity
     * @return Mono with ClassResponseDTO with schedules
     */
    private Mono<ClassResponseDTO> getClassSchedulesForClass(Class classEntity) {
        return classScheduleRepository.findByClassId(classEntity.getId())
                .map(this::mapToResponseDTO)
                .collectList()
                .map(schedules -> {
                    ClassResponseDTO classResponse = this.mapToResponseDTO(classEntity);
                    classResponse.setSchedules(schedules);
                    return classResponse;
                });
    }

    /**
     * Map from Class entity to ClassResponseDTO
     * @param classEntity Class entity
     * @return Mapped ClassResponseDTO
     */
    private ClassResponseDTO mapToResponseDTO(Class classEntity) {
        return modelMapper.map(classEntity, ClassResponseDTO.class);
    }

    /**
     * Map from ClassResponseDTO to Class entity
     * @param classCreateRequestDTO ClassCreateRequestDTO
     * @return Mapped Class entity
     */
    private Class mapToEntity(ClassCreateRequestDTO classCreateRequestDTO) {
        return modelMapper.map(classCreateRequestDTO, Class.class);
    }

    /**
     * Map from ClassSchedule entity to ClassScheduleResponseDTO
     * @param classSchedule ClassSchedule entity
     * @return Mapped ClassScheduleResponseDTO
     */
    private ClassScheduleResponseDTO mapToResponseDTO(ClassSchedule classSchedule) {
        return modelMapper.map(classSchedule, ClassScheduleResponseDTO.class);
    }

    /**
     * Map from ClassScheduleResponseDTO to ClassSchedule entity
     * @param classScheduleRequestDTO ClassScheduleRequestDTO
     * @return Mapped ClassSchedule entity
     */
    private ClassSchedule mapToEntity(ClassScheduleRequestDTO classScheduleRequestDTO) {
        return modelMapper.map(classScheduleRequestDTO, ClassSchedule.class);
    }

    /**
     * Implementation of isClassInSection from PlanningModuleClassContract
     * @param classId Class ID
     * @param sectionId Section ID
     * @return Mono<Boolean> true if the class belongs to the section, false otherwise
     */
    @Override
    public Mono<Boolean> isClassInSection(Long classId, Long sectionId) {
        return courseService.getCourseSectionId(classId)
            .map(courseSectionId -> courseSectionId.equals(sectionId));
    }

    /**
     * Filter a class by the section of the current logged-in user
     * @param classEntity Class entity to filter
     * @return Mono<Boolean> true if user can access this class, false otherwise
     */
    private Mono<Boolean> filterClassByUserSection(Class classEntity) {
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

                // Check if user has TEACHER role
                boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER"));

                if (isTeacher) {
                    // Teacher can see all classes
                    return Mono.just(true);
                }
                
                // For ROLE_SECTION users, filter by their section
                String userEmail = authentication.getName();
                
                return userService.getUserIdByEmail(userEmail)
                    .flatMap(sectionService::getSectionIdByUserId)
                    .flatMap(sectionId ->
                        courseService.getCourseSectionId(classEntity.getCourseId())
                            .map(classSectionId -> classSectionId.equals(sectionId))
                    )
                    .defaultIfEmpty(false);
            })
            .defaultIfEmpty(false); // If no security context, deny access
    }
}
