package co.edu.puj.secchub_backend.planning.contract;

import reactor.core.publisher.Mono;

public interface PlanningModuleClassContract {
    /**
     * Checks if a class with the given ID is of the section ID.
     * @param classId Class ID
     * @param sectionId Section ID
     * @return True if the class belongs to the section, false otherwise
     */
    Mono<Boolean> isClassInSection(Long classId, Long sectionId);
}
