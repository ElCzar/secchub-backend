package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.CourseDTO;
import co.edu.puj.secchub_backend.integration.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing courses.
 * Provides endpoints to create, query, update and partially update courses.
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Creates a new course.
     * @param courseDTO with course data
     * @return Created course with status 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<CourseDTO>> createCourse(@RequestBody CourseDTO courseDTO) {
        return courseService.createCourse(courseDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * List all existing courses.
     * @return Stream of courses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<CourseDTO>>> findAllCourses() {
        return courseService.findAllCourses()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get a course by its ID.
     * @param courseId Course ID
     * @return Course with the given ID
     */
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<CourseDTO>> findCourseById(@PathVariable Long courseId) {
        return courseService.findCourseById(courseId)
                .map(ResponseEntity::ok);
    }

    /**
     * Update an existing course given its ID.
     * @param courseId Course ID
     * @param courseDTO with updated course data
     * @return Updated course with ok status
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<CourseDTO>> updateCourse(@PathVariable Long courseId, @RequestBody CourseDTO courseDTO) {
        return courseService.updateCourse(courseId, courseDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Partially update a course given its ID.
     * @param courseId Course ID
     * @param updates Map of fields to update
     * @return Updated course with ok status
     */
    @PatchMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<CourseDTO>> patchCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> updates) {
        return courseService.patchCourse(courseId, updates)
                .map(ResponseEntity::ok);
    }
}
