package co.edu.puj.secchub_backend.integration.contract;

import reactor.core.publisher.Mono;

/**
 * Contract interface for student application-related operations in the integration module.
 */
public interface IntegrationModuleStudentApplicationContract {

    /**
     * Checks if a student application belongs to a specific user.
     * @param applicationId The ID of the student application.
     * @param sectionId The ID of the section.
     * @return true if the application belongs to the user, false otherwise.
     */
    Mono<Boolean> isApplicationOfSection(Long applicationId, Long sectionId);
}
