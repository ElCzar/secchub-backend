package co.edu.puj.secchub_backend.admin.contract;

import reactor.core.publisher.Mono;

/**
 * Contract interface for section-related operations in the admin module.
 * Allows for obtaining section information.
 */
public interface AdminModuleSectionContract {
    /**
     * Obtain section id by user id.
     * @param userId User ID
     * @return Section ID
     */
    Mono<Long> getSectionIdByUserId(Long userId);
}
