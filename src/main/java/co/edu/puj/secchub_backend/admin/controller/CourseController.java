package co.edu.puj.secchub_backend.admin.controller;

import co.edu.puj.secchub_backend.admin.dto.CourseRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.CourseResponseDTO;
import co.edu.puj.secchub_backend.admin.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

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
     * @param courseRequestDTO with course data
     * @return Created course with status 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<CourseResponseDTO>> createCourse(@RequestBody CourseRequestDTO courseRequestDTO) {
        return courseService.createCourse(courseRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * List all existing courses.
     * @return List of courses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Flux<CourseResponseDTO> findAllCourses() {
        return Flux.defer(() -> Flux.fromIterable(courseService.findAllCourses()))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    /**
     * Get a course by its ID.
     * @param courseId Course ID
     * @return Course with the given ID
     */
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<CourseResponseDTO>> findCourseById(@PathVariable Long courseId) {
        return courseService.findCourseById(courseId)
                .map(ResponseEntity::ok);
    }

    /**
     * Update an existing course given its ID.
     * @param courseId Course ID
     * @param courseRequestDTO with updated course data
     * @return Updated course with ok status
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<CourseResponseDTO>> updateCourse(@PathVariable Long courseId, @RequestBody CourseRequestDTO courseRequestDTO) {
        return courseService.updateCourse(courseId, courseRequestDTO)
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
    public Mono<ResponseEntity<CourseResponseDTO>> patchCourse(@PathVariable Long courseId, @RequestBody Map<String, Object> updates) {
        return courseService.patchCourse(courseId, updates)
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes a course by its ID.
     * @param courseId Course ID
     * @return ResponseEntity with no content status
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteCourse(@PathVariable Long courseId) {
        return courseService.deleteCourse(courseId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}