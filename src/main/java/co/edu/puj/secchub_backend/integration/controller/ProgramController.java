package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.service.AcademicRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for program-related endpoints.
 * Provides endpoints for program-specific academic request operations.
 */
@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
public class ProgramController {
    
    private final AcademicRequestService academicRequestService;

    /**
     * Gets all academic requests for a specific semester.
     * Filters by the currently authenticated user.
     * @param semesterId The semester ID to filter requests
     * @return List of academic requests for the specified semester
     */
    @GetMapping("/academic-requests")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER') or hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<AcademicRequestResponseDTO>>> getAcademicRequestsBySemester(
            @RequestParam Long semesterId) {
        return academicRequestService.findAcademicRequestsBySemesterAndUser(semesterId)
                .map(ResponseEntity::ok);
    }
}