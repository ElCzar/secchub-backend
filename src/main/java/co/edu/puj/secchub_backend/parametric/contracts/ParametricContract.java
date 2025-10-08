package co.edu.puj.secchub_backend.parametric.contracts;

import java.util.List;
import java.util.Map;

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
    StatusDTO getStatusByName(String name);
    
    /**
     * Finds a status name by its ID.
     * @param id the status ID
     * @return StatusDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getStatusNameById(Long id);
    
    /**
     * Checks if a status exists by name.
     * @param name the status name
     * @return true if exists, false otherwise
     */
    boolean statusExists(String name);
    
    /**
     * Finds a role by its name.
     * @param name the role name
     * @return RoleDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    RoleDTO getRoleByName(String name);
    
    /**
     * Gets role name by its ID.
     * @param id the role id
     * @return String containing the role name
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getRoleNameById(Long id);
    
    /**
     * Checks if a role exists by name.
     * @param name the role name
     * @return true if exists, false otherwise
     */
    boolean roleExists(String name);
    
    /**
     * Finds a document type by its name.
     * @param name the document type name
     * @return DocumentTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    DocumentTypeDTO getDocumentTypeByName(String name);
    
    /**
     * Finds a document type name by its ID.
     * @param id the document type ID
     * @return DocumentTypeDTO name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getDocumentTypeNameById(Long id);
    
    /**
     * Checks if a document type exists by name.
     * @param name the document type name
     * @return true if exists, false otherwise
     */
    boolean documentTypeExists(String name);
    
    /**
     * Finds an employment type by its name.
     * @param name the employment type name
     * @return EmploymentTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    EmploymentTypeDTO getEmploymentTypeByName(String name);
    
    /**
     * Finds an employment type name by its ID.
     * @param id the employment type ID
     * @return EmploymentTypeDTO name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getEmploymentTypeNameById(Long id);
    
    /**
     * Checks if an employment type exists by name.
     * @param name the employment type name
     * @return true if exists, false otherwise
     */
    boolean employmentTypeExists(String name);
    
    /**
     * Finds a modality by its name.
     * @param name the modality name
     * @return ModalityDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    ModalityDTO getModalityByName(String name);
    
    /**
     * Finds a modality name by its ID.
     * @param id the modality ID
     * @return Modality name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getModalityNameById(Long id);
    
    /**
     * Checks if a modality exists by name.
     * @param name the modality name
     * @return true if exists, false otherwise
     */
    boolean modalityExists(String name);

    // ClassroomType methods
    
    /**
     * Retrieves all available classroom types.
     * @return List of all classroom type DTOs
     */
    List<ClassroomTypeDTO> getAllClassroomTypes();
    
    /**
     * Finds a classroom type by its name.
     * @param name the classroom type name
     * @return ClassroomTypeDTO if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    ClassroomTypeDTO getClassroomTypeByName(String name);
    
    /**
     * Finds a classroom type name by its ID.
     * @param id the classroom type ID
     * @return Classroom type name if found
     * @throws co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException if not found
     */
    String getClassroomTypeNameById(Long id);
    
    /**
     * Checks if a classroom type exists by name.
     * @param name the classroom type name
     * @return true if exists, false otherwise
     */
    boolean classroomTypeExists(String name);
    
    /**
     * Gets a map of classroom type names to IDs for bulk operations.
     * @return Map with classroom type name as key and ID as value
     */
    Map<String, Long> getClassroomTypeNameToIdMap();
}