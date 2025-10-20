package co.edu.puj.secchub_backend.integration.controller;

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
        return Mono.fromCallable(() -> service.listCurrentSemesterTeacherClasses())
                .map(ResponseEntity::ok)
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Get all classes assigned to a teacher.
     * @param teacherId Teacher ID
     * @return List of classes assigned to the teacher
     */
    @GetMapping("/{teacherId}/classes")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getAllTeacherClasses(@PathVariable Long teacherId) {
        return Mono.fromCallable(() -> service.listAllTeacherClassByTeacher(teacherId))
                .map(ResponseEntity::ok)
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
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
        return Mono.fromCallable(() -> service.listTeacherClassByStatus(teacherId, statusId))
                .map(ResponseEntity::ok)
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Get teacherclass by the class ID
     * @param classId Class ID
     * @return List of teacher classes for the given class ID
     */
    @GetMapping("/classes/class/{classId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherClassResponseDTO>>> getTeacherClassByClassId(@PathVariable Long classId) {
        return Mono.fromCallable(() -> service.listTeacherClassByClassId(classId))
                .map(ResponseEntity::ok)
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
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

}
