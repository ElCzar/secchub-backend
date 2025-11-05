package co.edu.puj.secchub_backend.planning.controller;

import co.edu.puj.secchub_backend.planning.dto.ClassroomRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomResponseDTO;
import co.edu.puj.secchub_backend.planning.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing classroom resources.
 * Provides endpoints for CRUD operations following integration module patterns.
 */
@RestController
@RequestMapping("/classrooms")
@RequiredArgsConstructor
@Slf4j
public class ClassroomController {

    private final ClassroomService classroomService;

    /**
     * Get all classrooms.
     * @return Mono containing ResponseEntity with list of classroom response DTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassroomResponseDTO>>> getAllClassrooms() {
        return classroomService.getAllClassrooms()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get classroom by ID.
     * @param id the classroom ID
     * @return Mono containing ResponseEntity with classroom response DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<ClassroomResponseDTO>> getClassroomById(@PathVariable Long id) {
        return classroomService.getClassroomById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Create a new classroom.
     * @param classroomRequestDTO the classroom request data
     * @return Mono containing ResponseEntity with created classroom response DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ClassroomResponseDTO> createClassroom(
            @RequestBody ClassroomRequestDTO classroomRequestDTO) {
        return classroomService.createClassroom(classroomRequestDTO);
    }

    /**
     * Update an existing classroom.
     * @param id the classroom ID
     * @param classroomRequestDTO the updated classroom request data
     * @return Mono containing ResponseEntity with updated classroom response DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<ClassroomResponseDTO>> updateClassroom(
            @PathVariable Long id,
            @RequestBody ClassroomRequestDTO classroomRequestDTO) {
        return classroomService.updateClassroom(id, classroomRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete a classroom.
     * @param id the classroom ID
     * @return Mono containing ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteClassroom(@PathVariable Long id) {
        return classroomService.deleteClassroom(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Get classrooms by type.
     * @param typeId the classroom type ID
     * @return Mono containing ResponseEntity with list of classroom response DTOs
     */
    @GetMapping("/type/{typeId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<ClassroomResponseDTO>>> getClassroomsByType(@PathVariable Long typeId) {
        return classroomService.getClassroomsByType(typeId)
                .map(ResponseEntity::ok);
    }
}
