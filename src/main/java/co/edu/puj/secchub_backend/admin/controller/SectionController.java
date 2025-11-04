package co.edu.puj.secchub_backend.admin.controller;

import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for managing sections.
 * Provides endpoints to create, query, and update sections.
 */
@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    /**
     * List all existing sections.
     * @return List of sections
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<SectionResponseDTO>>> findAllSections() {
        return Mono.fromCallable(() -> ResponseEntity.ok(sectionService.findAllSections()));
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    @GetMapping("/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<SectionResponseDTO>> findSectionById(@PathVariable Long sectionId) {
        return sectionService.findSectionById(sectionId)
                .map(ResponseEntity::ok);
    }

    /**
     * Get sections managed by a specific user.
     * @param userId User ID
     * @return List of sections managed by the user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<SectionResponseDTO>>> findSectionsByUserId(@PathVariable Long userId) {
        return Mono.fromCallable(() -> ResponseEntity.ok(sectionService.findSectionsByUserId(userId)));
    }

    /**
     * Close planning for current user's section
     * @return Updated section
     */
    @PostMapping("/close-planning")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<ResponseEntity<SectionResponseDTO>> closePlanningForCurrentUser() {
        return sectionService.closePlanningForCurrentUser()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets if planning is closed for current user's section
     * @return true if planning is closed, false otherwise
     */
    @GetMapping("/is-planning-closed")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Boolean>> isPlanningClosedForCurrentUser() {
        return sectionService.isPlanningClosedForCurrentUser()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets planning status statistics (count of open and closed sections)
     * @return Map with openCount and closedCount
     */
    @GetMapping("/planning-status-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<java.util.Map<String, Integer>>> getPlanningStatusStats() {
        return sectionService.getPlanningStatusStats()
                .map(ResponseEntity::ok);
    }

    /**
     * Get sections summary for admin dashboard
     * Returns name, planning status, assigned classes count, and unconfirmed teachers count
     * @return List of section summaries
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<List<java.util.Map<String, Object>>>> getSectionsSummary() {
        return sectionService.getSectionsSummary()
                .map(ResponseEntity::ok);
    }
}