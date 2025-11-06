package co.edu.puj.secchub_backend.parametric.contracts;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contract interface for parametric values that can be accessed by other modules.
 * This interface defines the public API for retrieving lookup values and reference data.
 * All methods are cached for optimal performance.
 */
public interface ParametricContract {
    
    /**
     * Finds a status by its name.
     * @param name the status name
     * @return StatusDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<StatusDTO> getStatusByName(String name);
    
    /**
     * Finds a status name by its ID.
     * @param id the status ID
     * @return StatusDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getStatusNameById(Long id);
    
    /**
     * Checks if a status exists by name.
     * @param name the status name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> statusExists(String name);
    
    /**
     * Finds a role by its name.
     * @param name the role name
     * @return RoleDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<RoleDTO> getRoleByName(String name);
    
    /**
     * Gets role name by its ID.
     * @param id the role id
     * @return String containing the role name
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getRoleNameById(Long id);
    
    /**
     * Checks if a role exists by name.
     * @param name the role name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> roleExists(String name);
    
    /**
     * Finds a document type by its name.
     * @param name the document type name
     * @return DocumentTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<DocumentTypeDTO> getDocumentTypeByName(String name);
    
    /**
     * Finds a document type name by its ID.
     * @param id the document type ID
     * @return DocumentTypeDTO name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getDocumentTypeNameById(Long id);
    
    /**
     * Checks if a document type exists by name.
     * @param name the document type name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> documentTypeExists(String name);
    
    /**
     * Finds an employment type by its name.
     * @param name the employment type name
     * @return EmploymentTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<EmploymentTypeDTO> getEmploymentTypeByName(String name);
    
    /**
     * Finds an employment type name by its ID.
     * @param id the employment type ID
     * @return EmploymentTypeDTO name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getEmploymentTypeNameById(Long id);
    
    /**
     * Checks if an employment type exists by name.
     * @param name the employment type name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> employmentTypeExists(String name);
    
    /**
     * Finds a modality by its name.
     * @param name the modality name
     * @return ModalityDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<ModalityDTO> getModalityByName(String name);
    
    /**
     * Finds a modality name by its ID.
     * @param id the modality ID
     * @return Modality name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getModalityNameById(Long id);
    
    /**
     * Checks if a modality exists by name.
     * @param name the modality name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> modalityExists(String name);

    // ClassroomType methods
    
    /**
     * Retrieves all available classroom types.
     * @return Flux of all classroom type DTOs
     */
    Flux<ClassroomTypeDTO> getAllClassroomTypes();
    
    /**
     * Finds a classroom type by its name.
     * @param name the classroom type name
     * @return ClassroomTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<ClassroomTypeDTO> getClassroomTypeByName(String name);
    
    /**
     * Finds a classroom type name by its ID.
     * @param id the classroom type ID
     * @return Classroom type name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    Mono<String> getClassroomTypeNameById(Long id);
    
    /**
     * Checks if a classroom type exists by name.
     * @param name the classroom type name
     * @return true if exists, false otherwise
     */
    Mono<Boolean> classroomTypeExists(String name);
}