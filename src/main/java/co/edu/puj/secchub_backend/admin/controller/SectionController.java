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
}