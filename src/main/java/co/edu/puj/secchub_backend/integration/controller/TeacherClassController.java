package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.service.TeacherClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for HU17 (Professor availability confirmation).
 * Endpoints for listing, accepting and rejecting class assignments.
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherClassController {

    /** Service for teacher-class assignments. */
    private final TeacherClassService service;

    /**
     * Get all classes assigned to a teacher.
     * @param teacherId Teacher ID
     * @return Stream of classes assigned to the teacher
     */
    @GetMapping("/{teacherId}/classes")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<TeacherClass>> getAllTeacherClasses(@PathVariable Long teacherId) {
        return ResponseEntity.ok(service.listAllTeacherClassByTeacher(teacherId));
    }

    /**
     * Get classes assigned to a teacher filtered by status.
     * Status: Based on data in the database.
     * @param teacherId Teacher ID
     * @param statusId Status ID
     * @return Stream of classes assigned to the teacher with the given status
     */
    @GetMapping("/{teacherId}/classes/status/{statusId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<TeacherClass>> getTeacherClassesByStatus(
            @PathVariable Long teacherId,
            @PathVariable Long statusId) {
        return ResponseEntity.ok(service.listTeacherClassByStatus(teacherId, statusId));
    }

    /**
     * Accept a class assignment for a teacher with an optional observation.
     * @param teacherClassId Class ID
     * @param body Request body containing the observation
     * @return Updated class assignment
     */
    @PatchMapping("/classes/{teacherClassId}/accept")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<TeacherClass>> acceptTeacherClass(
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
    public Mono<ResponseEntity<TeacherClass>> rejectTeacherClass(
            @PathVariable Long teacherClassId,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = body != null ? body.get("observation") : null;
        return service.rejectTeacherClass(teacherClassId, observation)
                .map(ResponseEntity::ok);
    }

}
