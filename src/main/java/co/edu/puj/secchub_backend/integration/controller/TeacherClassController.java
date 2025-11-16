package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.TeacherClassAssignHoursRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassAssignHoursResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.integration.service.TeacherClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Controller for HU17 (Professor availability confirmation).
 * Endpoints for listing, accepting and rejecting class assignments.
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherClassController {
    private final TeacherClassService service;

    /**
     * Creates a new teacher-class assignment.
     * @param TeacherClassRequestDTO with assignment data
     * @return TeacherClassResponseDTO with created assignment
     */
    @PostMapping("/classes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<TeacherClassResponseDTO>> createTeacherClass(
            @RequestBody TeacherClassRequestDTO request) {
        return service.createTeacherClass(request)
                .map(ResponseEntity::ok);
    }

    /**
     * Get all teacher classes for the current semester.
     * @return List of teacher classes for the current semester
     */
    @GetMapping("/classes/current-semester")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getCurrentSemesterTeacherClasses() {
        return service.listCurrentSemesterTeacherClasses()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all teacher classes for a teacher in the current semester.
     * @param teacherId Teacher ID
     * @return List of teacher classes for the teacher in the current semester
     */
    @GetMapping("/classes/current-semester/{teacherId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getCurrentSemesterTeacherClassesByTeacher(@PathVariable Long teacherId) {
        return service.listCurrentSemesterTeacherClassesByTeacher(teacherId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all classes assigned to a teacher.
     * @param teacherId Teacher ID
     * @return List of classes assigned to the teacher
     */
    @GetMapping("/{teacherId}/classes")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getAllTeacherClasses(@PathVariable Long teacherId) {
        return service.listAllTeacherClassByTeacher(teacherId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all classes pending decision for current semester.
     * @return List of classes pending decision
     */
    @GetMapping("/classes/pending-decision")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getPendingDecisionClassesForCurrentSemester() {
        return service.listPendingDecisionClassesForCurrentSemester()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get classes assigned to a teacher filtered by status.
     * Status: Based on data in the database.
     * @param teacherId Teacher ID
     * @param statusId Status ID
     * @return List of classes assigned to the teacher with the given status
     */
    @GetMapping("/{teacherId}/classes/status/{statusId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getTeacherClassesByStatus(
            @PathVariable Long teacherId,
            @PathVariable Long statusId) {
        return service.listTeacherClassByStatus(teacherId, statusId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get teacherclass by the class ID
     * @param classId Class ID
     * @return List of teacher classes for the given class ID
     */
    @GetMapping("/classes/class/{classId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getTeacherClassByClassId(@PathVariable Long classId) {
        return service.listTeacherClassByClassId(classId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Accept a class assignment for a teacher with an optional observation.
     * @param teacherClassId Class ID
     * @param body Request body containing the observation
     * @return Updated class assignment
     */
    @PatchMapping("/classes/{teacherClassId}/accept")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<TeacherClassResponseDTO>> acceptTeacherClass(
            @PathVariable Long teacherClassId,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = body != null ? body.get("observation") : null;
        return service.acceptTeacherClass(teacherClassId, observation)
                .map(ResponseEntity::ok);
    }

    /**
     * Reject a class assignment for a teacher with an optional observation.
     * @param teacherClassId Class ID
     * @param body Request body containing the observation
     * @return Updated class assignment
     */
    @PatchMapping("/classes/{teacherClassId}/reject")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<TeacherClassResponseDTO>> rejectTeacherClass(
            @PathVariable Long teacherClassId,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = body != null ? body.get("observation") : null;
        return service.rejectTeacherClass(teacherClassId, observation)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete a teacher-class assignment by teacher ID and class ID.
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/classes/teacher/{teacherId}/class/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> deleteTeacherClass(
            @PathVariable Long teacherId,
            @PathVariable Long classId) {
        return service.deleteTeacherClassByTeacherAndClass(teacherId, classId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Get teacherclass by teacher ID and class ID
     * @param teacherId Teacher ID
     * @param classId Class ID
     * @return Teacher class assignment for the given teacher and class
     */
    @GetMapping("/{teacherId}/classes/{classId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<TeacherClassResponseDTO>> getTeacherClassByTeacherAndClass(
            @PathVariable Long teacherId, 
            @PathVariable Long classId) {
        return service.getTeacherClassByTeacherAndClass(teacherId, classId)
                .map(ResponseEntity::ok);
    }

    /**
     * Update the teaching dates (start and end date) for a teacher-class assignment.
     * @param teacherClassId Teacher-Class assignment ID
     * @param request Request body containing the new dates
     * @return Updated teacher-class assignment
     */
    @PatchMapping("/classes/{teacherClassId}/dates")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<TeacherClassResponseDTO>> updateTeachingDates(
            @PathVariable Long teacherClassId,
            @RequestBody TeacherClassRequestDTO request) {
        return service.updateTeachingDates(teacherClassId, request.getStartDate(), request.getEndDate())
                .map(ResponseEntity::ok);
    }

    /**
     * Get warning about extra hours when assigning work to a teacher.
     * @param teacherId Teacher ID
     * @param teacherClassAssignHoursRequestDTO Request body containing hours to assign
     * @return
     */
    @PostMapping("/{teacherId}/extra-hours-warning")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<TeacherClassAssignHoursResponseDTO>> getTeacherExtraHoursWarning(
        @PathVariable Long teacherId, 
        @RequestBody TeacherClassAssignHoursRequestDTO teacherClassAssignHoursRequestDTO) {
        return service.getTeacherExtraHoursWarning(teacherId, teacherClassAssignHoursRequestDTO)
                .map(ResponseEntity::ok);
    }
}
