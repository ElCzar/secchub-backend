package co.edu.puj.secchub_backend.admin.controller;

import co.edu.puj.secchub_backend.admin.dto.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherUpdateRequestDTO;
import co.edu.puj.secchub_backend.admin.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for managing teachers.
 * Provides endpoints for teacher retrieval, and updates.
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * Gets all teachers in the system.
     * @return List of all teachers
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherResponseDTO>>> getAllTeachers() {
        return Mono.fromCallable(() -> ResponseEntity.ok(teacherService.getAllTeachers()));
    }

    /**
     * Gets a teacher by their ID.
     * @param teacherId Teacher ID
     * @return Teacher with the specified ID
     */
    @GetMapping("/{teacherId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<TeacherResponseDTO>> getTeacherById(@PathVariable Long teacherId) {
        return teacherService.getTeacherById(teacherId)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates a teacher's employment type and max hours.
     * @param teacherId Teacher ID
     * @param teacherUpdateRequestDTO DTO with update data
     * @return Updated teacher
     */
    @PutMapping("/{teacherId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<TeacherResponseDTO>> updateTeacher(
            @PathVariable Long teacherId,
            @RequestBody TeacherUpdateRequestDTO teacherUpdateRequestDTO) {
        return teacherService.updateTeacher(teacherId, teacherUpdateRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a teacher by user ID.
     * @param userId User ID
     * @return Teacher associated with the user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER') or hasRole('ROLE_TEACHER')")
    public Mono<ResponseEntity<TeacherResponseDTO>> getTeacherByUserId(@PathVariable Long userId) {
        return teacherService.getTeacherByUserId(userId)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets teachers by employment type.
     * @param employmentTypeId Employment type ID
     * @return List of teachers with the specified employment type
     */
    @GetMapping("/employment-type/{employmentTypeId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherResponseDTO>>> getTeachersByEmploymentType(
            @PathVariable Long employmentTypeId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(teacherService.getTeachersByEmploymentType(employmentTypeId)));
    }

    /**
     * Gets teachers with minimum available hours.
     * @param minHours Minimum hours required
     * @return List of teachers with adequate hours
     */
    @GetMapping("/min-hours/{minHours}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<TeacherResponseDTO>>> getTeachersWithMinHours(@PathVariable Integer minHours) {
        return Mono.fromCallable(() -> ResponseEntity.ok(teacherService.getTeachersWithMinHours(minHours)));
    }
}