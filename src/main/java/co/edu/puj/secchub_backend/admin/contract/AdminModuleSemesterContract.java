package co.edu.puj.secchub_backend.admin.contract;

import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Contract interface for semester-related operations in the admin module.
 * Defines methods for obtaining semester information.
 */
public interface AdminModuleSemesterContract {
    /**
     * Obtains only the id of the current active semester.
     * @return Mono id of the current semester
     */
    Mono<Long> getCurrentSemesterId();
    
    /**
     * Obtains the current active semester with complete information.
     * @return Mono containing current semester information
     */
    Mono<SemesterResponseDTO> getCurrentSemester();

    /**
     * Obtains the semester information by its id.
     * @param semesterId the id of the semester to retrieve
     * @return Mono containing the semester information
     */
    Mono<SemesterResponseDTO> getSemesterById(Long semesterId);
}
