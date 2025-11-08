package co.edu.puj.secchub_backend.planning.controller;

import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.service.PlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for academic planning management.
 * Provides endpoints for managing classes and schedules following reactive patterns.
 */
@RestController
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;

    /**
     * Creates a new academic class.
     * @param classCreateRequestDTO the class data transfer object containing class information
     * @return ResponseEntity containing the created classResponseDTO and HTTP 201 status
     */
    @PostMapping("/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassResponseDTO>> createClass(@RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        return planningService.createClass(classCreateRequestDTO)
                .map(createdClass -> ResponseEntity.status(HttpStatus.CREATED).body(createdClass));
    }

    /**
     * Gets all academic classes for the current semester.
     * @return List of classes for the current semester with HTTP 200 status
     */
    @GetMapping("/classes/current-semester")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getCurrentSemesterClasses() {
        return planningService.findCurrentSemesterClasses()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all academic classes.
     * @return List of all classes
     */
    @GetMapping("/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getAllClasses() {
        return planningService.findAllClasses()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a specific class by ID.
     * @param classId Class ID
     * @return Class found
     */
    @GetMapping("/classes/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<ClassResponseDTO>> getClassById(@PathVariable Long classId) {
        return planningService.findClassById(classId)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an existing academic class.
     * @param classId Class ID
     * @param classCreateRequestDTO DTO with updated class data
     * @return Updated class
     */
    @PutMapping("/classes/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassResponseDTO>> updateClass(
            @PathVariable Long classId,
            @RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        return planningService.updateClass(classId, classCreateRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes an academic class by ID.
     * @param classId Class ID
     * @return Empty response with no content code 204
     */
    @DeleteMapping("/classes/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> deleteClass(@PathVariable Long classId) {
        return planningService.deleteClass(classId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Gets classes by course ID.
     * @param courseId Course ID
     * @return List of classes for the specified course with HTTP 200 status
     */
    @GetMapping("/classes/course/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getClassesByCourse(@PathVariable Long courseId) {
        return planningService.findClassesByCourse(courseId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets classes by section.
     * @param section Section number
     * @return List of classes for the specified section with HTTP 200 status
     */
    @GetMapping("/classes/section/{section}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getClassesBySection(@PathVariable Long section) {
        return planningService.findClassesBySection(section)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets classes by course for the current semester.
     * @param courseId Course ID
     * @return List of classes for the current semester and specified course with HTTP 200 status
     */
    @GetMapping("/classes/current-semester/course/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getCurrentSemesterClassesByCourse(@PathVariable Long courseId) {
        return planningService.findCurrentSemesterClassesByCourse(courseId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Adds a schedule to a class.
     * @param classId Class ID
     * @param classScheduleRequestDTO DTO with schedule data
     * @return Created schedule
     */
    @PostMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> addClassSchedule(
            @PathVariable Long classId,
            @RequestBody ClassScheduleRequestDTO classScheduleRequestDTO) {
        return planningService.addClassSchedule(classId, classScheduleRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets schedules associated with a class.
     * @param classId Class ID
     * @return List of schedules with HTTP 200 status
     */
    @GetMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassScheduleResponseDTO>>> getClassSchedules(@PathVariable Long classId) {
        return planningService.findClassSchedulesByClassId(classId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a specific class schedule by ID.
     * @param scheduleId Schedule ID
     * @return Class schedule found with HTTP 200 status
     */
    @GetMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> getClassScheduleById(@PathVariable Long scheduleId) {
        return planningService.findClassScheduleById(scheduleId)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates a specific class schedule.
     * @param scheduleId Schedule ID
     * @param classScheduleRequestDTO DTO with updated data
     * @return Updated schedule
     */
    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> updateClassSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ClassScheduleRequestDTO classScheduleRequestDTO) {
        return planningService.updateClassSchedule(scheduleId, classScheduleRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes a class schedule by ID.
     * @param scheduleId Schedule ID
     * @return Empty response with no content code 204
     */
    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> deleteClassSchedule(@PathVariable Long scheduleId) {
        return planningService.deleteClassSchedule(scheduleId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Partially updates a class schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Partially updated schedule with HTTP 200 status
     */
    @PatchMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> patchClassSchedule(
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {
        return planningService.patchClassSchedule(scheduleId, updates)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets schedules by classroom ID.
     * @param classroomId Classroom ID
     * @return List of schedules for the specified classroom with HTTP 200 status
     */
    @GetMapping("/schedules/classroom/{classroomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassScheduleResponseDTO>>> getSchedulesByClassroom(@PathVariable Long classroomId) {
        return planningService.findClassSchedulesByClassroom(classroomId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets schedules by day.
     * @param day Day of the week
     * @return List of schedules for the specified day with HTTP 200 status
     */
    @GetMapping("/schedules/day/{day}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassScheduleResponseDTO>>> getSchedulesByDay(@PathVariable String day) {
        return planningService.findClassSchedulesByDay(day)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets schedules with disability accommodations.
     * @param disability True to find schedules with disability accommodations
     * @return List of schedules with disability considerations with HTTP 200 status
     */
    @GetMapping("/schedules/disability/{disability}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassScheduleResponseDTO>>> getSchedulesByDisability(@PathVariable Boolean disability) {
        return planningService.findClassSchedulesByDisability(disability)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get classes by semester id.
     * @param semesterId Semester ID
     * @return List of classes for the specified semester with HTTP 200 status
     */
    @GetMapping("/classes/semester/{semesterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getClassesBySemester(@PathVariable Long semesterId) {
        return planningService.findClassesBySemester(semesterId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Duplicate planning from one semester to another.
     * @param sourceSemesterId Source semester ID
     * @param targetSemesterId Target semester ID
     * @return List of duplicated classes
     */
    @PostMapping("/duplicate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> duplicateSemesterPlanning(@RequestParam Long sourceSemesterId, @RequestParam Long targetSemesterId) {
        return planningService.duplicateSemesterPlanning(sourceSemesterId, targetSemesterId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Duplicate planning from classIds to current semester.
     * @param classIds List of class IDs to duplicate
     * @return List of duplicated classes
     */
    @PostMapping("/duplicate/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> duplicateClassPlanning(@RequestBody List<Long> classIds) {
        return planningService.duplicateClassPlanning(classIds)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Apply planning from a previous semester to the current semester
     * @param sourceSemesterId Source semester ID
     * @return List of classes applied to the current semester
     */
    @PostMapping("/semesters/{sourceSemesterId}/apply-to-current")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> applySemesterPlanningToCurrent(@PathVariable Long sourceSemesterId) {
        return planningService.applySemesterPlanningToCurrent(sourceSemesterId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all current semester classes without assigned classroom
     * @return List of current semester classes without assigned classroom
     */
    @GetMapping("/classes/current-semester/no-classroom")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getCurrentSemesterClassesWithoutClassroom() {
        return planningService.findClassesWithoutClassroomAssigned()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all current semester classes without at least one teacher assigned
     * @return List of current semester classes without at least one teacher assigned
     */
    @GetMapping("/classes/current-semester/no-teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassResponseDTO>>> getCurrentSemesterClassesWithoutTeacher() {
        return planningService.findClassesWithoutTeacherAssigned()
                .collectList()
                .map(ResponseEntity::ok);
    }
}
