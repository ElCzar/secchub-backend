package co.edu.puj.secchub_backend.admin.contract;

import reactor.core.publisher.Mono;

/**
 * Contract interface for course-related operations in the admin module.
 * Defines methods for obtaining course information.
 */
public interface AdminModuleCourseContract {
    /**
     * Obtains the name of a course by its ID.
     * @param courseId the ID of the course
     * @return the name of the course, or "Curso sin nombre" if not found
     */
    Mono<String> getCourseName(Long courseId);

    /**
     * Obtains the section ID associated with a given course ID.
     * @param courseId the ID of the course
     * @return the section ID associated with the course, or null if not found
     */
    Mono<Long> getCourseSectionId(Long courseId);
}