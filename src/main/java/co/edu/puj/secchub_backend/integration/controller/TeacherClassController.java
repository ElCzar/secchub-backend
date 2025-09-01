package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.service.TeacherClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for HU17 (Professor availability confirmation).
 * Endpoints for listing, accepting and rejecting class assignments.
 */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherClassController {

    private final TeacherClassService service;

    /**
     * Get all classes assigned to a teacher (any status).
     */
    @GetMapping("/{teacherId}/classes")
    public ResponseEntity<List<TeacherClass>> getAllClasses(@PathVariable Long teacherId) {
        return ResponseEntity.ok(service.listAllByTeacher(teacherId));
    }

    /**
     * Get classes assigned to a teacher filtered by status.
     * Status: 1=Pending, 2=Accepted, 3=Rejected.
     */
    @GetMapping("/{teacherId}/classes/status/{statusId}")
    public ResponseEntity<List<TeacherClass>> getClassesByStatus(
            @PathVariable Long teacherId,
            @PathVariable Long statusId) {
        return ResponseEntity.ok(service.listByStatus(teacherId, statusId));
    }

    /**
     * Accept a class assignment for a teacher with an optional observation.
     */
    @PatchMapping("/classes/{teacherClassId}/accept")
    public ResponseEntity<TeacherClass> acceptClass(
            @PathVariable Long teacherClassId,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = body != null ? body.get("observation") : null;
        return ResponseEntity.ok(service.acceptClass(teacherClassId, observation));
    }

    /**
     * Reject a class assignment for a teacher with an optional observation.
     */
    @PatchMapping("/classes/{teacherClassId}/reject")
    public ResponseEntity<TeacherClass> rejectClass(
            @PathVariable Long teacherClassId,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = body != null ? body.get("observation") : null;
        return ResponseEntity.ok(service.rejectClass(teacherClassId, observation));
    }

}
