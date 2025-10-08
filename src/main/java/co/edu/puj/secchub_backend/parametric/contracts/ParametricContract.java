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
     * Retrieves all available statuses.
     * @return List of all status DTOs
     */
    List<StatusDTO> getAllStatuses();
    
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
     * Retrieves all available roles.
     * @return List of all role DTOs
     */
    List<RoleDTO> getAllRoles();
    
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
     * Gets a map of status names to IDs for bulk operations.
     * @return Map with status name as key and ID as value
     */
    Map<String, Long> getStatusNameToIdMap();
    
    /**
     * Gets a map of role names to IDs for bulk operations.
     * @return Map with role name as key and ID as value
     */
    Map<String, Long> getRoleNameToIdMap();
}