package co.edu.puj.secchub_backend.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.service.SemesterService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing semesters.
 * Provides endpoints to create, query, update and delete semesters.
 */
@Controller
@RequestMapping("/semesters")
@RequiredArgsConstructor
public class SemesterController {
    private final SemesterService semesterService;

    /**
     * Creates the following semester.
     * @param semesterRequestDTO with semester data
     * @return Created semester with status 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<SemesterResponseDTO>> createSemester(@RequestBody SemesterRequestDTO semesterRequestDTO) {
        return semesterService.createSemester(semesterRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Obtains current semester.
     * @return Current semester with status 200
     */
    @RequestMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<SemesterResponseDTO>> getCurrentSemester() {
        return semesterService.getCurrentSemester()
                .map(ResponseEntity::ok);
    }

    /**
     * Obtains all semesters.
     * @return All semesters with status 200
     */
    @RequestMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<SemesterResponseDTO>>> getAllSemesters() {
        return semesterService.getAllSemesters()
                .map(ResponseEntity::ok);
    }
}
