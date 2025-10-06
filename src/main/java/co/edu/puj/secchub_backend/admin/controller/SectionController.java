package co.edu.puj.secchub_backend.admin.controller;

import co.edu.puj.secchub_backend.admin.dto.SectionDTO;
import co.edu.puj.secchub_backend.admin.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * Creates a new section.
     * @param sectionDTO with section data
     * @return Created section with status 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<SectionDTO>> createSection(@RequestBody SectionDTO sectionDTO) {
        return sectionService.createSection(sectionDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * List all existing sections.
     * @return List of sections
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SectionDTO> findAllSections() {
        return sectionService.findAllSections();
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    @GetMapping("/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<SectionDTO>> findSectionById(@PathVariable Long sectionId) {
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
    public List<SectionDTO> findSectionsByUserId(@PathVariable Long userId) {
        return sectionService.findSectionsByUserId(userId);
    }
}