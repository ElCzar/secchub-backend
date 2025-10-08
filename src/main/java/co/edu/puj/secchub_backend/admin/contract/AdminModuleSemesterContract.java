package co.edu.puj.secchub_backend.admin.contract;

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
}
