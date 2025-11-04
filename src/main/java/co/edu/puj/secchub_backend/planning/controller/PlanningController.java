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
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {
    /**
     * Endpoint para advertencia de horas extra de un docente.
     * Entrada: teacherId (path), workHoursToAssign (body)
     * Salida: teacherName, maxHours, currentAssignedHours, workHoursToAssign, excessHours
     */
    @PostMapping("/teachers/{teacherId}/extra-hours-warning")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<?>> getTeacherExtraHoursWarning(@PathVariable Long teacherId, @RequestBody Map<String, Object> body) {
        return Mono.fromCallable(() -> {
            int workHoursToAssign = body.get("workHoursToAssign") instanceof Number ? ((Number) body.get("workHoursToAssign")).intValue() : 0;
            var jdbcTemplate = planningService.getJdbcTemplate();
            // Obtener nombre y max_hours del docente
            String teacherSql = "SELECT u.name, t.max_hours FROM teacher t JOIN users u ON t.user_id = u.id WHERE t.id = ?";
            var teacherResult = jdbcTemplate.queryForList(teacherSql, teacherId);
            if (teacherResult.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var teacherRow = teacherResult.get(0);
            String teacherName = String.valueOf(teacherRow.get("name"));
            int maxHours = teacherRow.get("max_hours") != null ? ((Number) teacherRow.get("max_hours")).intValue() : 0;
            // Obtener horas ya asignadas
            String assignedSql = "SELECT COALESCE(SUM(work_hours),0) AS assigned FROM teacher_class WHERE teacher_id = ?";
            var assignedResult = jdbcTemplate.queryForList(assignedSql, teacherId);
            int currentAssignedHours = assignedResult.isEmpty() ? 0 : ((Number) assignedResult.get(0).get("assigned")).intValue();
            int total = currentAssignedHours + workHoursToAssign;
            int excessHours = Math.max(total - maxHours, 0);
            Map<String, Object> result = Map.of(
                "teacherName", teacherName,
                "maxHours", maxHours,
                "currentAssignedHours", currentAssignedHours,
                "workHoursToAssign", workHoursToAssign,
                "excessHours", excessHours
            );
            return ResponseEntity.ok(result);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    /**
     * Get max hours for a teacher by ID
     */
    @GetMapping("/teachers/{teacherId}/max-hours")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<?>> getTeacherMaxHours(@PathVariable Long teacherId) {
        return Mono.fromCallable(() -> {
            String sql = "SELECT t.max_hours, u.name, t.employment_type_id FROM teacher t JOIN users u ON t.user_id = u.id WHERE t.id = ?";
            String sumSql = "SELECT COALESCE(SUM(work_hours + COALESCE(full_time_extra_hours,0)),0) AS assigned FROM teacher_class WHERE teacher_id = ? AND decision = 1";
            var jdbcTemplate = planningService.getJdbcTemplate();
            var result = jdbcTemplate.queryForList(sql, teacherId);
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var row = result.get(0);
            var sumResult = jdbcTemplate.queryForList(sumSql, teacherId);
            int assignedHours = sumResult.isEmpty() ? 0 : ((Number) sumResult.get(0).get("assigned")).intValue();
            int maxHours = row.get("max_hours") != null ? ((Number) row.get("max_hours")).intValue() : 0;
            int exceedsMaxHours = assignedHours >= maxHours ? 1 : 0;
            return ResponseEntity.ok(Map.of(
                "teacherId", teacherId,
                "maxHours", maxHours,
                "name", row.get("name"),
                "employmentTypeId", row.get("employment_type_id"),
                "assignedHours", assignedHours,
                "exceedsMaxHours", exceedsMaxHours
            ));
        }).subscribeOn(Schedulers.boundedElastic());
    }
    /**
     * Aplica solo las clases seleccionadas al semestre actual.
     * Recibe el ID del semestre origen y un array de IDs de clases a duplicar.
     * @param request Map con semesterId y classIds
     * @return mensaje y cantidad de clases copiadas
     */
    @PostMapping("/semesters/apply-selected")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Map<String, Object>>> applySelectedClasses(@RequestBody Map<String, Object> request) {
        Long semesterId = request.get("semesterId") instanceof Number ? ((Number) request.get("semesterId")).longValue() : null;
        List<Integer> classIdsRaw = (List<Integer>) request.get("classIds");
        List<Long> classIds = classIdsRaw != null ? classIdsRaw.stream().map(Integer::longValue).toList() : List.of();
        int applied = planningService.applySelectedClassesToCurrentSemester(semesterId, classIds);
        Map<String, Object> result = Map.of(
            "message", "Clases seleccionadas duplicadas exitosamente",
            "classesApplied", applied
        );
        return Mono.just(ResponseEntity.ok(result));
    }
    /**
     * Aplica clases filtradas al semestre actual.
     * Recibe un arreglo de clases, valida y copia cada una al semestre actual.
     * @param classes lista de clases con datos completos
     * @return mensaje y cantidad de clases copiadas
     */

    @GetMapping("/classes/section-chief/without-room")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<List<ClassResponseDTO>> getClassesWithoutAssignedRoomForSectionChief(org.springframework.security.core.Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        Long userId = securityModuleUserContract.getUserIdByEmail(email);
        return Mono.fromCallable(() -> planningService.findClassesWithoutAssignedRoomForSectionChief(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }
    /**
     * Gets classes without assigned teachers for the authenticated section chief and current semester.
     * The userId is extracted from the JWT of the authenticated user.
     * @return List of classes without assigned teachers
     */
    private final co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract securityModuleUserContract;

    @GetMapping("/classes/section-chief/without-teacher")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<List<ClassResponseDTO>> getClassesWithoutAssignedTeacherForSectionChief(org.springframework.security.core.Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        Long userId = securityModuleUserContract.getUserIdByEmail(email);
        return Mono.fromCallable(() -> planningService.findClassesWithoutAssignedTeacherForSectionChief(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
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
     * @return Partially updated schedule
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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

    /**
     * Get semester planning preview - shows what classes would be copied from a previous semester
     * TEMPORAL: Sin autenticación para pruebas
     */
    @GetMapping("/semesters/{semesterId}/preview")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")  // Comentado temporalmente
    public Mono<ResponseEntity<Map<String, Object>>> getSemesterPlanningPreview(@PathVariable Long semesterId) {
        return Mono.fromCallable(() -> {
            List<ClassResponseDTO> classes = planningService.findClassesBySemesterId(semesterId);
            Map<String, Object> preview = Map.of(
                "semesterId", semesterId,
                "totalClasses", classes.size(),
                "classes", classes
            );
            return ResponseEntity.ok(preview);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Apply planning from a previous semester to the current semester
     * TEMPORAL: Sin autenticación para pruebas
     */
    @PostMapping("/semesters/{sourceSemesterId}/apply-to-current")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")  // Comentado temporalmente
    public Mono<ResponseEntity<Map<String, Object>>> applySemesterPlanningToCurrent(@PathVariable Long sourceSemesterId) {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = planningService.applySemesterPlanningToCurrent(sourceSemesterId);
            return ResponseEntity.ok(result);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get past semesters that are available for planning duplication.
     * Returns only semesters that have already ended (excluding current semester).
     * TEMPORAL: Sin autenticación para pruebas
     */
    @GetMapping("/semesters/past")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")  // Comentado temporalmente
    public Mono<ResponseEntity<List<Map<String, Object>>>> getPastSemesters() {
        return Mono.fromCallable(() -> {
            List<Map<String, Object>> pastSemesters = planningService.getPastSemesters();
            return ResponseEntity.ok(pastSemesters);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get schedule conflicts (teachers and classrooms with overlapping schedules).
     * Returns conflicts for the current semester and the section of the authenticated user.
     */
    @GetMapping("/conflicts")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO>> getScheduleConflicts(
            org.springframework.security.core.Authentication authentication) {
        return Mono.fromCallable(() -> {
            // Get user email from JWT
            String email = (String) authentication.getPrincipal();
            
            // Get user_id from email
            String userSql = "SELECT id FROM users WHERE email = ?";
            var jdbcTemplate = planningService.getJdbcTemplate();
            List<Map<String, Object>> userResult = jdbcTemplate.queryForList(userSql, email);
            
            if (userResult.isEmpty()) {
                return List.<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO>of(); // User not found, return empty list
            }
            
            Long userId = ((Number) userResult.get(0).get("id")).longValue();
            
            // Get section_id from user_id
            String sectionSql = "SELECT id FROM section WHERE user_id = ?";
            List<Map<String, Object>> sectionResult = jdbcTemplate.queryForList(sectionSql, userId);
            
            if (sectionResult.isEmpty()) {
                return List.<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO>of(); // Section not found, return empty list
            }
            
            Long sectionId = ((Number) sectionResult.get(0).get("id")).longValue();
            
            // Detect conflicts
            return planningService.detectScheduleConflicts(sectionId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ADMIN ENDPOINTS - Global view without section filtering
     */

    /**
     * Get all classes without assigned classroom (admin view - all sections)
     * @return List of classes without classroom across all sections
     */
    @GetMapping("/admin/classes/without-room")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<List<ClassResponseDTO>> getClassesWithoutRoomAdmin() {
        return Mono.fromCallable(() -> planningService.getClassesWithoutRoomGlobal())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get all classes without assigned teacher (admin view - all sections)
     * @return List of classes without teacher across all sections
     */
    @GetMapping("/admin/classes/without-teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<List<ClassResponseDTO>> getClassesWithoutTeacherAdmin() {
        return Mono.fromCallable(() -> planningService.getClassesWithoutTeacherGlobal())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get all schedule conflicts (admin view - all sections)
     * @return List of all schedule conflicts across all sections
     */
    @GetMapping("/admin/conflicts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO>> getScheduleConflictsAdmin() {
        return Mono.fromCallable(() -> planningService.detectScheduleConflictsGlobal())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get all pending teacher confirmations (admin view - all sections)
     * @return List of teacher-class assignments awaiting confirmation across all sections
     */
    @GetMapping("/admin/pending-confirmations")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<List<Map<String, Object>>> getPendingConfirmationsAdmin() {
        return Mono.fromCallable(() -> planningService.getPendingConfirmationsGlobal())
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get dashboard alerts summary for admin (all sections)
     * @return Dashboard alerts with counts across all sections
     */
    @GetMapping("/admin/dashboard/alerts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getDashboardAlertsAdmin() {
        return Mono.fromCallable(() -> {
            var jdbcTemplate = planningService.getJdbcTemplate();
            
            // Get current semester
            String currentSemesterSql = "SELECT id, end_date FROM semester WHERE is_current = 1";
            List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
            
            if (semesterResult.isEmpty()) {
                Map<String, Object> emptyResponse = new java.util.HashMap<>();
                emptyResponse.put("missingTeachers", 0);
                emptyResponse.put("missingRooms", 0);
                emptyResponse.put("pendingConfirmations", 0);
                emptyResponse.put("scheduleConflicts", 0);
                emptyResponse.put("daysLeft", 0);
                return ResponseEntity.ok(emptyResponse);
            }
            
            Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
            java.sql.Date endDate = (java.sql.Date) semesterResult.get(0).get("end_date");
            
            // Calculate days left
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                endDate.toLocalDate()
            );
            
            // Count classes without teacher (global)
            // Classes without any teacher_class record
            String missingTeachersSql = """
                SELECT COUNT(DISTINCT c.id) as count
                FROM class c
                LEFT JOIN teacher_class tc ON c.id = tc.class_id
                WHERE c.semester_id = ?
                  AND tc.teacher_id IS NULL
                """;
            List<Map<String, Object>> missingTeachersResult = jdbcTemplate.queryForList(missingTeachersSql, currentSemesterId);
            int missingTeachers = missingTeachersResult.isEmpty() ? 0 : ((Number) missingTeachersResult.get(0).get("count")).intValue();
            
            // Count classes without room (global)
            // Classes that have at least one in-person schedule (modality_id = 1) without a classroom
            String missingRoomsSql = """
                SELECT COUNT(DISTINCT c.id) as count
                FROM class c
                WHERE c.semester_id = ?
                  AND EXISTS (
                    SELECT 1 FROM class_schedule cs 
                    WHERE cs.class_id = c.id 
                      AND cs.modality_id = 1
                      AND cs.classroom_id IS NULL
                  )
                """;
            List<Map<String, Object>> missingRoomsResult = jdbcTemplate.queryForList(missingRoomsSql, currentSemesterId);
            int missingRooms = missingRoomsResult.isEmpty() ? 0 : ((Number) missingRoomsResult.get(0).get("count")).intValue();
            
            // Count pending teacher confirmations (global)
            // status_id = 4 means pending confirmation
            String pendingConfirmationsSql = """
                SELECT COUNT(*) as count
                FROM teacher_class tc
                INNER JOIN class c ON tc.class_id = c.id
                WHERE c.semester_id = ?
                  AND tc.status_id = 4
                """;
            List<Map<String, Object>> pendingResult = jdbcTemplate.queryForList(pendingConfirmationsSql, currentSemesterId);
            int pendingConfirmations = pendingResult.isEmpty() ? 0 : ((Number) pendingResult.get(0).get("count")).intValue();
            
            // Count schedule conflicts (global)
            List<co.edu.puj.secchub_backend.planning.dto.ScheduleConflictDTO> conflicts = 
                planningService.detectScheduleConflictsGlobal();
            int scheduleConflicts = conflicts.size();
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("missingTeachers", missingTeachers);
            response.put("missingRooms", missingRooms);
            response.put("pendingConfirmations", pendingConfirmations);
            response.put("scheduleConflicts", scheduleConflicts);
            response.put("daysLeft", Math.max(0, daysLeft));
            response.put("endDate", endDate.toLocalDate().toString()); // Add endDate field
            
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
