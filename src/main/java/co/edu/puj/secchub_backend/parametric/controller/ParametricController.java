package co.edu.puj.secchub_backend.parametric.controller;

import co.edu.puj.secchub_backend.parametric.contracts.ClassroomTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.DocumentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.EmploymentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.ModalityDTO;
import co.edu.puj.secchub_backend.parametric.contracts.RoleDTO;
import co.edu.puj.secchub_backend.parametric.contracts.StatusDTO;
import co.edu.puj.secchub_backend.parametric.service.ParametricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controller for parametric endpoints to retrieve lookup values.
 * Provides access to all parametric data: Status, Role, DocumentType, EmploymentType, and Modality.
 */
@RestController
@RequestMapping("/parametric")
@RequiredArgsConstructor
public class ParametricController {

    private final ParametricService parametricService;

    /**
     * Gets all statuses.
     * @return List of all status DTOs
     */
    @GetMapping("/statuses")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<StatusDTO>>> getAllStatuses() {
        return parametricService.getAllStatuses()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a status by its ID.
     * @param id Status ID
     * @return Status name
     */
    @GetMapping("/statuses/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getStatusById(@PathVariable Long id) {
        return parametricService.getStatusNameById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all roles.
     * @return List of all role DTOs
     */
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<RoleDTO>>> getAllRoles() {
        return parametricService.getAllRoles()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a role by its ID.
     * @param id Role ID
     * @return Role name
     */
    @GetMapping("/roles/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getRoleById(@PathVariable Long id) {
        return parametricService.getRoleNameById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all document types.
     * @return List of all document type DTOs
     */
    @GetMapping("/document-types")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<DocumentTypeDTO>>> getAllDocumentTypes() {
        return parametricService.getAllDocumentTypes()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a document type by its ID.
     * @param id Document type ID
     * @return Document type name
     */
    @GetMapping("/document-types/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getDocumentTypeById(@PathVariable Long id) {
        return parametricService.getDocumentTypeNameById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all employment types.
     * @return List of all employment type DTOs
     */
    @GetMapping("/employment-types")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<EmploymentTypeDTO>>> getAllEmploymentTypes() {
        return parametricService.getAllEmploymentTypes()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets an employment type by its ID.
     * @param id Employment type ID
     * @return Employment type name
     */
    @GetMapping("/employment-types/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getEmploymentTypeById(@PathVariable Long id) {
        return parametricService.getEmploymentTypeNameById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all modalities.
     * @return List of all modality DTOs
     */
    @GetMapping("/modalities")
    @PreAuthorize("isAuthenticated() or hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<ModalityDTO>>> getAllModalities() {
        return parametricService.getAllModalities()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a modality by its ID.
     * @param id Modality ID
     * @return Modality name
     */
    @GetMapping("/modalities/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getModalityById(@PathVariable Long id) {
        return parametricService.getModalityNameById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets all classroom types.
     * @return List of all classroom type DTOs
     */
    @GetMapping("/classroom-types")
    @PreAuthorize("isAuthenticated() or hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<ClassroomTypeDTO>>> getAllClassroomTypes() {
        return parametricService.getAllClassroomTypes()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a classroom type by its ID.
     * @param id Classroom type ID
     * @return Classroom type name
     */
    @GetMapping("/classroom-types/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<String>> getClassroomTypeById(@PathVariable Long id) {
        return parametricService.getClassroomTypeNameById(id)
                .map(ResponseEntity::ok);
    }
}