package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.Class;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Class entity.
 * Provides data access operations for class management in the planning module.
 */
@Repository
public interface ClassRepository extends R2dbcRepository<Class, Long> {
    
    /**
     * Find classes by semester ID.
     * @param semesterId the semester ID
     * @return flux of classes in the specified semester
     */
    Flux<Class> findBySemesterId(Long semesterId);
    
    /**
     * Find classes by course ID.
     * @param courseId the course ID
     * @return flux of classes for the specified course
     */
    Flux<Class> findByCourseId(Long courseId);
    
    /**
     * Find classes by section.
     * @param section the section number
     * @return flux of classes for the specified section
     */
    Flux<Class> findBySection(Long section);
    
    /**
     * Find classes by semester and course.
     * @param semesterId the semester ID
     * @param courseId the course ID
     * @return flux of classes for the specified semester and course
     */
    Flux<Class> findBySemesterIdAndCourseId(Long semesterId, Long courseId);
}