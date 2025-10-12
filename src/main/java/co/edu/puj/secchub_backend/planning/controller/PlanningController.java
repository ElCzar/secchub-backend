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
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for academic planning management.
 * Provides endpoints for managing classes and schedules following reactive patterns.
 */
@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;

    /**
     * Creates a new academic class.
     * @param classCreateRequestDTO the class data transfer object containing class information
     * @return ResponseEntity containing the created classResponseDTO and HTTP 201 status
     */
    @PostMapping("/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<ClassResponseDTO>> createClass(@RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        return planningService.createClass(classCreateRequestDTO)
                .map(createdClass -> ResponseEntity.status(HttpStatus.CREATED).body(createdClass));
    }

    /**
     * Gets all academic classes for the current semester.
     * @return List of classes for the current semester
     */
    @GetMapping("/classes/current-semester")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassResponseDTO>> getCurrentSemesterClasses() {
        return Mono.fromCallable(() -> planningService.findCurrentSemesterClasses())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets all academic classes.
     * @return List of all classes
     */
    @GetMapping("/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassResponseDTO>> getAllClasses() {
        return Mono.fromCallable(() -> planningService.findAllClasses())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a specific class by ID.
     * @param classId Class ID
     * @return Class found
     */
    @GetMapping("/classes/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<Void>> deleteClass(@PathVariable Long classId) {
        return planningService.deleteClass(classId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Gets classes by course ID.
     * @param courseId Course ID
     * @return List of classes for the specified course
     */
    @GetMapping("/classes/course/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassResponseDTO>> getClassesByCourse(@PathVariable Long courseId) {
        return Mono.fromCallable(() -> planningService.findClassesByCourse(courseId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets classes by section.
     * @param section Section number
     * @return List of classes for the specified section
     */
    @GetMapping("/classes/section/{section}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassResponseDTO>> getClassesBySection(@PathVariable Long section) {
        return Mono.fromCallable(() -> planningService.findClassesBySection(section))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets classes by course for the current semester.
     * @param courseId Course ID
     * @return List of classes for the current semester and specified course
     */
    @GetMapping("/classes/current-semester/course/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassResponseDTO>> getCurrentSemesterClassesByCourse(@PathVariable Long courseId) {
        return Mono.fromCallable(() -> planningService.findCurrentSemesterClassesByCourse(courseId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Adds a schedule to a class.
     * @param classId Class ID
     * @param classScheduleRequestDTO DTO with schedule data
     * @return Created schedule
     */
    @PostMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> addClassSchedule(
            @PathVariable Long classId,
            @RequestBody ClassScheduleRequestDTO classScheduleRequestDTO) {
        return planningService.addClassSchedule(classId, classScheduleRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets schedules associated with a class.
     * @param classId Class ID
     * @return List of schedules
     */
    @GetMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassScheduleResponseDTO>> getClassSchedules(@PathVariable Long classId) {
        return Mono.fromCallable(() -> planningService.findClassSchedulesByClassId(classId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a specific class schedule by ID.
     * @param scheduleId Schedule ID
     * @return Class schedule found
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<Void>> deleteClassSchedule(@PathVariable Long scheduleId) {
        return planningService.deleteClassSchedule(scheduleId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Partially updates a class schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Partially updated schedule
     */
    @PatchMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<ClassScheduleResponseDTO>> patchClassSchedule(
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {
        return planningService.patchClassSchedule(scheduleId, updates)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets schedules by classroom ID.
     * @param classroomId Classroom ID
     * @return List of schedules for the specified classroom
     */
    @GetMapping("/schedules/classroom/{classroomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassScheduleResponseDTO>> getSchedulesByClassroom(@PathVariable Long classroomId) {
        return Mono.fromCallable(() -> planningService.findClassSchedulesByClassroom(classroomId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules by day.
     * @param day Day of the week
     * @return List of schedules for the specified day
     */
    @GetMapping("/schedules/day/{day}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassScheduleResponseDTO>> getSchedulesByDay(@PathVariable String day) {
        return Mono.fromCallable(() -> planningService.findClassSchedulesByDay(day))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules with disability accommodations.
     * @param disability True to find schedules with disability accommodations
     * @return List of schedules with disability considerations
     */
    @GetMapping("/schedules/disability/{disability}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<ClassScheduleResponseDTO>> getSchedulesByDisability(@PathVariable Boolean disability) {
        return Mono.fromCallable(() -> planningService.findClassSchedulesByDisability(disability))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get classes by semester id.
     */
    @GetMapping("/classes/semester/{semesterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public List<ClassResponseDTO> getClassesBySemester(@PathVariable Long semesterId) {
        return planningService.findClassesBySemester(semesterId);
    }

    /**
     * Validate a class payload before creating/updating. Returns conflicts if any.
     */
    @PostMapping("/classes/validate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<Map<String, Object>>> validateClass(@RequestBody ClassCreateRequestDTO classCreateRequestDTO) {
        return planningService.validateClass(classCreateRequestDTO)
                .map(result -> ResponseEntity.ok(result));
    }

    /**
     * Find potential schedule conflicts for a given classroom and day.
     * Example: /api/planning/schedules/conflicts?classroomId=1&day=Monday
     */
    @GetMapping("/schedules/conflicts")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public List<ClassScheduleResponseDTO> getScheduleConflicts(@RequestParam Long classroomId, @RequestParam String day) {
        return planningService.findConflictingSchedulesByClassroomAndDay(classroomId, day);
    }

    /**
     * Duplicate planning from one semester to another.
     * Query params: sourceSemesterId, targetSemesterId
     */
    @PostMapping("/duplicate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public List<ClassResponseDTO> duplicateSemesterPlanning(@RequestParam Long sourceSemesterId, @RequestParam Long targetSemesterId) {
        return planningService.duplicateSemesterPlanning(sourceSemesterId, targetSemesterId);
    }

    /**
     * Basic utilization statistics per semester (simple aggregation).
     */
    @GetMapping("/statistics/utilization/{semesterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Map<String, Object> getUtilizationStatistics(@PathVariable Long semesterId) {
        return planningService.getUtilizationStatistics(semesterId);
    }

    /**
     * Stub endpoint to expose available teachers. Frontend expects this under planning.
     */
    @GetMapping("/teachers/available")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<Map<String, Object>>> getAvailableTeachers(@RequestParam(required = false, defaultValue = "0") Integer requiredHours) {
        return Mono.fromCallable(() -> planningService.getAvailableTeachers(requiredHours))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // --------------------------
    // Teacher-assignment shim (for frontend dev compatibility)
    // These endpoints mirror the shapes the frontend expects under /api/teacher-assignments
    // and delegate to the PlanningService stub data.
    // --------------------------

    @GetMapping("/teacher-assignments/class/{classId}/teachers")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<Map<String, Object>>> getAssignedTeachersForClass(@PathVariable Long classId) {
        return Mono.fromCallable(() -> planningService.getAvailableTeachers(0))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/teacher-assignments/assign")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<Map<String, Object>>> assignTeacherToClass(
            @RequestParam Long teacherId,
            @RequestParam Long classId,
            @RequestParam Integer workHours,
            @RequestParam(required = false) String observation
    ) {
        return Mono.fromCallable(() -> {
            var available = planningService.getAvailableTeachers(0);
            var found = available.stream()
                    .filter(m -> Long.valueOf(String.valueOf(m.get("id"))).equals(teacherId))
                    .findFirst()
                    .orElse(Map.of("id", teacherId, "name", "Docente", "lastName", "Sín nombre", "availableHours", 0));
            return ResponseEntity.ok(found);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/teacher-assignments/class/{classId}/teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PLANNER')")
    public Mono<ResponseEntity<Map<String, Object>>> changeTeacherForClassViaPlanning(
            @PathVariable Long classId,
            @RequestParam Long newTeacherId,
            @RequestParam Integer workHours,
            @RequestParam(required = false) String observation
    ) {
        return Mono.fromCallable(() -> {
            var available = planningService.getAvailableTeachers(0);
            var found = available.stream()
                    .filter(m -> Long.valueOf(String.valueOf(m.get("id"))).equals(newTeacherId))
                    .findFirst()
                    .orElse(Map.of("id", newTeacherId, "name", "Docente", "lastName", "Sín nombre", "availableHours", 0));
            return ResponseEntity.ok(found);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/teacher-assignments/class/{classId}/available-teachers")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<Map<String, Object>>> getAvailableTeachersForClassShim(@PathVariable Long classId, @RequestParam(required = false, defaultValue = "0") Integer requiredHours) {
        return Mono.fromCallable(() -> planningService.getAvailableTeachers(requiredHours))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
