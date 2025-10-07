package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.StudentApplicationRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationResponseDTO;
import co.edu.puj.secchub_backend.integration.service.StudentApplicationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;


/**
 * REST controller for managing StudentApplication monitoring applications.
 * Provides endpoints to create, query, approve and reject monitoring applications.
 */
@RestController
@RequestMapping("/student-applications")
@RequiredArgsConstructor
public class StudentApplicationController {
    
    private final StudentApplicationService studentApplicationService;

    /**
     * Creates a new student application.
     * @param studentApplicationRequestDTO DTO with request data
     * @return StudentApplicationResponseDTO with the created application
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Mono<ResponseEntity<StudentApplicationResponseDTO>> createStudentApplication(@RequestBody StudentApplicationRequestDTO studentApplicationRequestDTO) {
        return studentApplicationService.createStudentApplication(studentApplicationRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets all student applications for the current semester.
     * @return List of StudentApplicationResponseDTOs with applications for the current semester
     */
    @GetMapping("/current-semester")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplicationResponseDTO>> getCurrentSemesterStudentApplications() {
        return ResponseEntity.ok(studentApplicationService.listCurrentSemesterStudentApplications());
    }

    /**
     * Gets all student applications.
     * @return List of StudentApplicationResponseDTOs with applications
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplicationResponseDTO>> getAllStudentApplications() {
        return ResponseEntity.ok(studentApplicationService.listAllStudentApplications());
    }

    /**
     * Gets a student application by its ID.
     * @param studentApplicationId Application ID
     * @return StudentApplicationResponseDTO with the found application
     */
    @GetMapping("/{studentApplicationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<StudentApplicationResponseDTO>> getStudentApplicationById(@PathVariable Long studentApplicationId) {
        return studentApplicationService.findStudentApplicationById(studentApplicationId)
                .map(ResponseEntity::ok);
    }

    /**
     * Approves a student application.
     * @param studentApplicationId Application ID
     * @param statusId Approval status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> approveStudentApplication(@PathVariable Long studentApplicationId) {
        return studentApplicationService.approveStudentApplication(studentApplicationId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Rejects a student application.
     * @param studentApplicationId Application ID
     * @param statusId Rejection status ID
     * @return Response with ok status
     */
    @PatchMapping("/{studentApplicationId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> rejectStudentApplication(@PathVariable Long studentApplicationId) {
        return studentApplicationService.rejectStudentApplication(studentApplicationId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    /**
     * Gets student applications by status.
     * @param statusId Status ID
     * @return List of StudentApplicationResponseDTOs with applications in that status
     */
    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplicationResponseDTO>> getStudentApplicationByStatus(@PathVariable Long statusId) {
        return ResponseEntity.ok(studentApplicationService.listStudentApplicationsByStatus(statusId));
    }

    /**
     * Gets student applications for a specific section.
     * @param sectionId Section ID
     * @return List of StudentApplicationResponseDTOs with applications in that section
     */
    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<StudentApplicationResponseDTO>> getStudentApplicationBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(studentApplicationService.listStudentApplicationsForSection(sectionId));
    }
}
