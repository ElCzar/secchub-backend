package co.edu.puj.secchub_backend.parametric.service;

import co.edu.puj.secchub_backend.parametric.contracts.*;
import co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException;
import co.edu.puj.secchub_backend.parametric.model.DocumentType;
import co.edu.puj.secchub_backend.parametric.model.EmploymentType;
import co.edu.puj.secchub_backend.parametric.model.Role;
import co.edu.puj.secchub_backend.parametric.model.Status;
import co.edu.puj.secchub_backend.parametric.repository.DocumentTypeRepository;
import co.edu.puj.secchub_backend.parametric.repository.EmploymentTypeRepository;
import co.edu.puj.secchub_backend.parametric.repository.RoleRepository;
import co.edu.puj.secchub_backend.parametric.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for parametric values with caching support.
 * All methods are cached for optimal performance since parametric values
 * are frequently accessed but rarely change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParametricService implements ParametricContract {
    private final StatusRepository statusRepository;
    private final RoleRepository roleRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final EmploymentTypeRepository employmentTypeRepository;

    @Override
    @Cacheable(value = "all-statuses")
    public List<StatusDTO> getAllStatuses() {
        log.debug("Loading all statuses from database");
        return statusRepository.findAll()
                .stream()
                .map(this::mapToStatusDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "status-by-name", key = "#name")
    public StatusDTO getStatusByName(String name) {
        log.debug("Looking up status by name: {}", name);
        return statusRepository.findByName(name)
                .map(this::mapToStatusDTO)
                .orElseThrow(() -> new ParametricValueNotFoundException("Status not found: " + name));
    }

    @Override
    @Cacheable(value = "status-id-to-name", key = "#id")
    public String getStatusNameById(Long id) {
        log.debug("Looking up status name for ID: {}", id);
        return statusRepository.findById(id)
                .map(Status::getName)
                .orElseThrow(() -> new ParametricValueNotFoundException("Status not found: " + id));
    }

    @Override
    @Cacheable(value = "status-exists", key = "#name")
    public boolean statusExists(String name) {
        return statusRepository.existsByName(name);
    }

    @Override
    @Cacheable(value = "all-roles")
    public List<RoleDTO> getAllRoles() {
        log.debug("Loading all roles from database");
        return roleRepository.findAll()
                .stream()
                .map(this::mapToRoleDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "role-by-name", key = "#name")
    public RoleDTO getRoleByName(String name) {
        log.debug("Looking up role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ParametricValueNotFoundException("Role not found: " + name));
        return mapToRoleDTO(role);
    }

    @Override
    @Cacheable(value = "role-id-by-name", key = "#name")
    public String getRoleNameById(Long id) {
        log.debug("Looking up role name for ID: {}", id);
        return roleRepository.findById(id)
                .map(Role::getName)
                .orElseThrow(() -> new ParametricValueNotFoundException("Role not found: " + id));
    }

    @Override
    @Cacheable(value = "role-exists", key = "#name")
    public boolean roleExists(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    @Cacheable(value = "status-name-to-id-map")
    public Map<String, Long> getStatusNameToIdMap() {
        log.debug("Building status name to ID map");
        return statusRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Status::getName, Status::getId));
    }

    @Override
    @Cacheable(value = "role-name-to-id-map")
    public Map<String, Long> getRoleNameToIdMap() {
        log.debug("Building role name to ID map");
        return roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getName, Role::getId));
    }

    private StatusDTO mapToStatusDTO(Status status) {
        return StatusDTO.builder()
                .id(status.getId())
                .name(status.getName())
                .build();
    }

    private RoleDTO mapToRoleDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    // DocumentType implementations

    @Override
    @Cacheable(value = "all-document-types")
    public List<DocumentTypeDTO> getAllDocumentTypes() {
        log.debug("Loading all document types from database");
        return documentTypeRepository.findAll()
                .stream()
                .map(this::mapToDocumentTypeDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "document-type-by-name", key = "#name")
    public DocumentTypeDTO getDocumentTypeByName(String name) {
        log.debug("Looking up document type by name: {}", name);
        return documentTypeRepository.findByName(name)
                .map(this::mapToDocumentTypeDTO)
                .orElseThrow(() -> new ParametricValueNotFoundException("Document type not found: " + name));
    }

    @Override
    @Cacheable(value = "document-type-id-to-name", key = "#id")
    public String getDocumentTypeNameById(Long id) {
        log.debug("Looking up document type name for ID: {}", id);
        return documentTypeRepository.findById(id)
                .map(DocumentType::getName)
                .orElseThrow(() -> new ParametricValueNotFoundException("Document type not found: " + id));
    }

    @Override
    @Cacheable(value = "document-type-exists", key = "#name")
    public boolean documentTypeExists(String name) {
        return documentTypeRepository.existsByName(name);
    }

    @Override
    @Cacheable(value = "document-type-name-to-id-map")
    public Map<String, Long> getDocumentTypeNameToIdMap() {
        log.debug("Building document type name to ID map");
        return documentTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(DocumentType::getName, DocumentType::getId));
    }

    // EmploymentType implementations

    @Override
    @Cacheable(value = "all-employment-types")
    public List<EmploymentTypeDTO> getAllEmploymentTypes() {
        log.debug("Loading all employment types from database");
        return employmentTypeRepository.findAll()
                .stream()
                .map(this::mapToEmploymentTypeDTO)
                .toList();
    }

    @Override
    @Cacheable(value = "employment-type-by-name", key = "#name")
    public EmploymentTypeDTO getEmploymentTypeByName(String name) {
        log.debug("Looking up employment type by name: {}", name);
        return employmentTypeRepository.findByName(name)
                .map(this::mapToEmploymentTypeDTO)
                .orElseThrow(() -> new ParametricValueNotFoundException("Employment type not found: " + name));
    }

    @Override
    @Cacheable(value = "employment-type-id-to-name", key = "#id")
    public String getEmploymentTypeNameById(Long id) {
        log.debug("Looking up employment type name for ID: {}", id);
        return employmentTypeRepository.findById(id)
                .map(EmploymentType::getName)
                .orElseThrow(() -> new ParametricValueNotFoundException("Employment type not found: " + id));
    }

    @Override
    @Cacheable(value = "employment-type-exists", key = "#name")
    public boolean employmentTypeExists(String name) {
        return employmentTypeRepository.existsByName(name);
    }

    @Override
    @Cacheable(value = "employment-type-name-to-id-map")
    public Map<String, Long> getEmploymentTypeNameToIdMap() {
        log.debug("Building employment type name to ID map");
        return employmentTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(EmploymentType::getName, EmploymentType::getId));
    }

    // Private mapping methods

    private DocumentTypeDTO mapToDocumentTypeDTO(DocumentType documentType) {
        return DocumentTypeDTO.builder()
                .id(documentType.getId())
                .name(documentType.getName())
                .build();
    }

    private EmploymentTypeDTO mapToEmploymentTypeDTO(EmploymentType employmentType) {
        return EmploymentTypeDTO.builder()
                .id(employmentType.getId())
                .name(employmentType.getName())
                .build();
    }
}