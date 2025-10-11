package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Class entity.
 * Provides data access operations for class management in the planning module.
 */
@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    
    /**
     * Find classes by semester ID.
     * @param semesterId the semester ID
     * @return list of classes in the specified semester
     */
    List<Class> findBySemesterId(Long semesterId);
    
    /**
     * Find classes by course ID.
     * @param courseId the course ID
     * @return list of classes for the specified course
     */
    List<Class> findByCourseId(Long courseId);
    
    /**
     * Find classes by section.
     * @param section the section number
     * @return list of classes for the specified section
     */
    List<Class> findBySection(Long section);
    
    /**
     * Find classes by semester and course.
     * @param semesterId the semester ID
     * @param courseId the course ID
     * @return list of classes for the specified semester and course
     */
    List<Class> findBySemesterIdAndCourseId(Long semesterId, Long courseId);
}