package co.edu.puj.secchub_backend.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.service.SemesterService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing semesters.
 * Provides endpoints to create, query, update and delete semesters.
 */
@RestController
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
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<SemesterResponseDTO>> getCurrentSemester() {
        return semesterService.getCurrentSemester()
                .map(ResponseEntity::ok);
    }

    /**
     * Obtains semester by year and period.
     * @param year The year of the semester
     * @param period The period of the semester (1 or 2)
     * @return Semester matching the criteria with status 200
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER') or hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<SemesterResponseDTO>> getSemesterByYearAndPeriod(
            @RequestParam Integer year, 
            @RequestParam Integer period) {
        return semesterService.getSemesterByYearAndPeriod(year, period)
                .map(ResponseEntity::ok);
    }

    /**
     * Obtains all semesters.
     * @return All semesters with status 200
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<SemesterResponseDTO>>> getAllSemesters() {
        return semesterService.getAllSemesters()
                .map(ResponseEntity::ok);
    }
}
