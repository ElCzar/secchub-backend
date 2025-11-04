package co.edu.puj.secchub_backend.parametric.service;

import co.edu.puj.secchub_backend.parametric.contracts.*;
import co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException;
import co.edu.puj.secchub_backend.parametric.model.*;
import co.edu.puj.secchub_backend.parametric.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParametricService implements ParametricContract {
    private final ModelMapper modelMapper;
    private final StatusRepository statusRepository;
    private final RoleRepository roleRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final ModalityRepository modalityRepository;
    private final ClassroomTypeRepository classroomTypeRepository;

    /* ---------------- STATUS ---------------- */

    @Cacheable("all-statuses")
    public Flux<StatusDTO> getAllStatuses() {
        log.debug("Loading all statuses from database");
        return statusRepository.findAll()
                .map(this::mapToStatusDTO);
    }

    @Cacheable(value = "status-by-name", key = "#name")
    public Mono<StatusDTO> getStatusByName(String name) {
        log.debug("Looking up status by name: {}", name);
        return statusRepository.findByName(name)
                .map(this::mapToStatusDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Status not found: " + name)));
    }

    @Cacheable(value = "status-id-to-name", key = "#id")
    public Mono<String> getStatusNameById(Long id) {
        log.debug("Looking up status name for ID: {}", id);
        return statusRepository.findById(id)
                .map(Status::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Status not found: " + id)));
    }

    @Cacheable(value = "status-exists", key = "#name")
    public Mono<Boolean> statusExists(String name) {
        return statusRepository.existsByName(name);
    }

    /* ---------------- ROLE ---------------- */

    @Cacheable("all-roles")
    public Flux<RoleDTO> getAllRoles() {
        log.debug("Loading all roles from database");
        return roleRepository.findAll()
                .map(this::mapToRoleDTO);
    }

    @Cacheable(value = "role-by-name", key = "#name")
    public Mono<RoleDTO> getRoleByName(String name) {
        log.debug("Looking up role by name: {}", name);
        return roleRepository.findByName(name)
                .map(this::mapToRoleDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Role not found: " + name)));
    }

    @Cacheable(value = "role-id-by-name", key = "#id")
    public Mono<String> getRoleNameById(Long id) {
        log.debug("Looking up role name for ID: {}", id);
        return roleRepository.findById(id)
                .map(Role::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Role not found: " + id)));
    }

    @Cacheable(value = "role-exists", key = "#name")
    public Mono<Boolean> roleExists(String name) {
        return roleRepository.existsByName(name);
    }

    /* ---------------- DOCUMENT TYPE ---------------- */

    @Cacheable("all-document-types")
    public Flux<DocumentTypeDTO> getAllDocumentTypes() {
        log.debug("Loading all document types from database");
        return documentTypeRepository.findAll()
                .map(this::mapToDocumentTypeDTO);
    }

    @Cacheable(value = "document-type-by-name", key = "#name")
    public Mono<DocumentTypeDTO> getDocumentTypeByName(String name) {
        log.debug("Looking up document type by name: {}", name);
        return documentTypeRepository.findByName(name)
                .map(this::mapToDocumentTypeDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Document type not found: " + name)));
    }

    @Cacheable(value = "document-type-id-to-name", key = "#id")
    public Mono<String> getDocumentTypeNameById(Long id) {
        log.debug("Looking up document type name for ID: {}", id);
        return documentTypeRepository.findById(id)
                .map(DocumentType::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Document type not found: " + id)));
    }

    @Cacheable(value = "document-type-exists", key = "#name")
    public Mono<Boolean> documentTypeExists(String name) {
        return documentTypeRepository.existsByName(name);
    }

    /* ---------------- EMPLOYMENT TYPE ---------------- */

    @Cacheable("all-employment-types")
    public Flux<EmploymentTypeDTO> getAllEmploymentTypes() {
        log.debug("Loading all employment types from database");
        return employmentTypeRepository.findAll()
                .map(this::mapToEmploymentTypeDTO);
    }

    @Cacheable(value = "employment-type-by-name", key = "#name")
    public Mono<EmploymentTypeDTO> getEmploymentTypeByName(String name) {
        log.debug("Looking up employment type by name: {}", name);
        return employmentTypeRepository.findByName(name)
                .map(this::mapToEmploymentTypeDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Employment type not found: " + name)));
    }

    @Cacheable(value = "employment-type-id-to-name", key = "#id")
    public Mono<String> getEmploymentTypeNameById(Long id) {
        log.debug("Looking up employment type name for ID: {}", id);
        return employmentTypeRepository.findById(id)
                .map(EmploymentType::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Employment type not found: " + id)));
    }

    @Cacheable(value = "employment-type-exists", key = "#name")
    public Mono<Boolean> employmentTypeExists(String name) {
        return employmentTypeRepository.existsByName(name);
    }

    /* ---------------- MODALITY ---------------- */

    @Cacheable("all-modalities")
    public Flux<ModalityDTO> getAllModalities() {
        log.debug("Loading all modalities from database");
        return modalityRepository.findAll()
                .map(this::mapToModalityDTO);
    }

    @Cacheable(value = "modality-by-name", key = "#name")
    public Mono<ModalityDTO> getModalityByName(String name) {
        log.debug("Looking up modality by name: {}", name);
        return modalityRepository.findByName(name)
                .map(this::mapToModalityDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Modality not found: " + name)));
    }

    @Cacheable(value = "modality-id-to-name", key = "#id")
    public Mono<String> getModalityNameById(Long id) {
        log.debug("Looking up modality name for ID: {}", id);
        return modalityRepository.findById(id)
                .map(Modality::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Modality not found: " + id)));
    }

    @Cacheable(value = "modality-exists", key = "#name")
    public Mono<Boolean> modalityExists(String name) {
        return modalityRepository.existsByName(name);
    }

    /* ---------------- CLASSROOM TYPE ---------------- */

    @Cacheable("all-classroom-types")
    public Flux<ClassroomTypeDTO> getAllClassroomTypes() {
        log.debug("Loading all classroom types from database");
        return classroomTypeRepository.findAll()
                .map(this::mapToClassroomTypeDTO);
    }

    @Cacheable(value = "classroom-type-by-name", key = "#name")
    public Mono<ClassroomTypeDTO> getClassroomTypeByName(String name) {
        log.debug("Looking up classroom type by name: {}", name);
        return classroomTypeRepository.findByName(name)
                .map(this::mapToClassroomTypeDTO)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Classroom type not found: " + name)));
    }

    @Cacheable(value = "classroom-type-id-to-name", key = "#id")
    public Mono<String> getClassroomTypeNameById(Long id) {
        log.debug("Looking up classroom type name for ID: {}", id);
        return classroomTypeRepository.findById(id)
                .map(ClassroomType::getName)
                .switchIfEmpty(Mono.error(new ParametricValueNotFoundException("Classroom type not found: " + id)));
    }

    @Cacheable(value = "classroom-type-exists", key = "#name")
    public Mono<Boolean> classroomTypeExists(String name) {
        return classroomTypeRepository.existsByName(name);
    }

    /* ---------------- MAPPERS ---------------- */

    private StatusDTO mapToStatusDTO(Status s) {
        return modelMapper.map(s, StatusDTO.class);
    }

    private RoleDTO mapToRoleDTO(Role r) {
        return modelMapper.map(r, RoleDTO.class);
    }

    private DocumentTypeDTO mapToDocumentTypeDTO(DocumentType d) {
        return modelMapper.map(d, DocumentTypeDTO.class);
    }

    private EmploymentTypeDTO mapToEmploymentTypeDTO(EmploymentType e) {
        return modelMapper.map(e, EmploymentTypeDTO.class);
    }

    private ModalityDTO mapToModalityDTO(Modality m) {
        return modelMapper.map(m, ModalityDTO.class);
    }

    private ClassroomTypeDTO mapToClassroomTypeDTO(ClassroomType c) {
        return modelMapper.map(c, ClassroomTypeDTO.class);
    }
}
