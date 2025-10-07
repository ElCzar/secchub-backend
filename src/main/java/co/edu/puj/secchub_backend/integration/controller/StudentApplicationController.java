package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.StudentApplicationDTO;
import co.edu.puj.secchub_backend.integration.model.StudentApplication;
import co.edu.puj.secchub_backend.integration.service.StudentApplicationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * REST controller for managing StudentApplication monitoring applications.
 * Provides endpoints to create, query, approve and reject monitoring applications.
 */
@RestController
@RequestMapping("/StudentApplications-applications")
@RequiredArgsConstructor
public class StudentApplicationController {
    
    private final StudentApplicationService studentApplicationService;

    /**
     * Creates a new monitoring request.
     * @param StudentApplicationApplicationDTO DTO with request data
     * @return StudentApplication with the created request
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_StudentApplication')")
    public Mono<ResponseEntity<StudentApplication>> createStudentApplication(@RequestBody StudentApplicationDTO studentApplicationDTO) {
        return studentApplicationService.createStudentApplication(studentApplicationDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets all monitoring requests.
     * @return Stream of StudentApplications with requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<StudentApplication>> getAllStudentApplications() {
        return ResponseEntity.ok(studentApplicationService.listAllStudentApplications());
    }

    /**
     * Gets a StudentApplication application by its ID.
     * @param StudentApplicationApplicationId Application ID
     * @return StudentApplication with the found request
     */
    @GetMapping("/{studentApplicationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<StudentApplication>> getStudentApplicationById(@PathVariable Long studentApplicationId) {
        return studentApplicationService.findStudentApplicationById(studentApplicationId)
                .map(ResponseEntity::ok);
    }

    /**
     * Approves a monitoring request.
     * @param StudentApplicationApplicationId Application ID
     * @param statusId Approval status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> approveStudentApplication(@PathVariable Long studentApplicationId, @RequestParam Long statusId) {
        return studentApplicationService.approveStudentApplication(studentApplicationId, statusId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Rejects a monitoring request.
     * @param StudentApplicationApplicationId Application ID
     * @param statusId Rejection status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> rejectStudentApplication(@PathVariable Long studentApplicationId, @RequestParam Long statusId) {
        return studentApplicationService.rejectStudentApplication(studentApplicationId, statusId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Gets monitoring requests by status.
     * @param statusId Status ID
     * @return Stream of StudentApplications with requests in that status
     */
    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplication>> getStudentApplicationByStatus(@PathVariable Long statusId) {
        return ResponseEntity.ok(studentApplicationService.listStudentApplicationsByStatus(statusId));
    }

    /**
     * Gets monitoring requests for a specific section.
     * @param sectionId Section ID
     * @return Stream of StudentApplications with requests in that section
     */
    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplication>> getStudentApplicationBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(studentApplicationService.listStudentApplicationsForSection(sectionId));
    }
}
