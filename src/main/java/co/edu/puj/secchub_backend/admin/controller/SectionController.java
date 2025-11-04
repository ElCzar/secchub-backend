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
        return sectionService.findAllSections()
                .collectList()
                .map(ResponseEntity::ok);
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
    public Mono<ResponseEntity<SectionResponseDTO>> findSectionsByUserId(@PathVariable Long userId) {
        return sectionService.findSectionsByUserId(userId)
                .map(ResponseEntity::ok);
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
}