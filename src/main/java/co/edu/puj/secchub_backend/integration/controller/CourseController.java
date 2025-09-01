package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.model.Course;
import co.edu.puj.secchub_backend.integration.dto.CourseDTO;
import co.edu.puj.secchub_backend.integration.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course.
     */
    @PostMapping
    public ResponseEntity<CourseDTO> create(@RequestBody CourseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(dto));
    }

    /**
     * List all courses.
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> findAll() {
        return ResponseEntity.ok(courseService.findAll());
    }

    /**
     * Get a course by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    /**
     * Update an existing course.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }


        /**
     * Partially update a course (PATCH).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CourseDTO> patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(courseService.patch(id, updates));
    }

}
