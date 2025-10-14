package co.edu.puj.secchub_backend.admin.contract;

import java.util.List;
import java.util.Map;

/**
 * Contract interface for semester-related operations in the admin module.
 * Defines methods for obtaining semester information.
 */
public interface AdminModuleSemesterContract {
    /**
     * Obtains only the id of the current active semester.
     * @return id of the current semester
     */
    Long getCurrentSemesterId();
    
    /**
     * Obtains all semesters that have already ended (past semesters).
     * Excludes the current active semester.
     * @return List of past semesters with id, name, and other basic info
     */
    List<Map<String, Object>> getPastSemesters();
}
