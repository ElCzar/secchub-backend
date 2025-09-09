package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.StudentApplicationDTO;
import co.edu.puj.secchub_backend.integration.model.Student;
import co.edu.puj.secchub_backend.integration.service.StudentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * REST controller for managing student monitoring applications.
 * Provides endpoints to create, query, approve and reject monitoring applications.
 */
@RestController
@RequestMapping("/students-applications")
@RequiredArgsConstructor
public class StudentApplicationController {

    /** Service for student application business logic. */
    private final StudentApplicationService service;

    /**
     * Creates a new monitoring request.
     * @param studentApplicationDTO DTO with request data
     * @return Student with the created request
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Mono<ResponseEntity<Student>> createStudentApplication(@RequestBody StudentApplicationDTO studentApplicationDTO) {
        return service.createStudentApplication(studentApplicationDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets all monitoring requests.
     * @return Stream of students with requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<Student>> getAllStudentApplications() {
        return ResponseEntity.ok(service.listAllStudentApplication());
    }

    /**
     * Gets a student application by its ID.
     * @param studentApplicationId Application ID
     * @return Student with the found request
     */
    @GetMapping("/{studentApplicationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Student>> getStudentApplicationById(@PathVariable Long studentApplicationId) {
        return service.findStudentApplicationById(studentApplicationId)
                .map(ResponseEntity::ok);
    }

    /**
     * Approves a monitoring request.
     * @param studentApplicationId Application ID
     * @param statusId Approval status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> approveStudentApplication(@PathVariable Long studentApplicationId, @RequestParam Long statusId) {
        return service.approveStudentApplication(studentApplicationId, statusId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Rejects a monitoring request.
     * @param studentApplicationId Application ID
     * @param statusId Rejection status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> rejectStudentApplication(@PathVariable Long studentApplicationId, @RequestParam Long statusId) {
        return service.rejectStudentApplication(studentApplicationId, statusId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Gets monitoring requests by status.
     * @param statusId Status ID
     * @return Stream of students with requests in that status
     */
    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<Student>> getStudentApplicationsByStatus(@PathVariable Long statusId) {
        return ResponseEntity.ok(service.listStudentApplicationsByStatus(statusId));
    }

    /**
     * Gets monitoring requests for a specific section.
     * @param sectionId Section ID
     * @return Stream of students with requests in that section
     */
    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<Student>> getStudentApplicationsForSection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(service.listStudentApplicationsForSection(sectionId));
    }
}
