package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.Class;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.Query;
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

    /**
     * Find classes by semester and no classroom assigned.
     * @param semesterId the semester ID
     * @return flux of classes for the specified semester without assigned classroom
     */
    @Query("SELECT DISTINCT c.* FROM class c " +
        "INNER JOIN class_schedule cs ON c.id = cs.class_id " +
        "WHERE c.semester_id = :semesterId " +
        "AND cs.classroom_id IS NULL")
    Flux<Class> findBySemesterIdAndNoClassroomAssigned(Long semesterId);

    /**
     * Find classes by semester and no confirmed teacher assigned.
     * Returns classes that either have no teacher_class records or only have teacher_class records with status != 3 (Confirmed).
     * @param semesterId the semester ID
     * @return flux of classes for the specified semester without confirmed teacher assigned
     */
    @Query("SELECT c.* FROM class c " +
        "WHERE c.semester_id = :semesterId " +
        "AND NOT EXISTS (" +
        "  SELECT 1 FROM teacher_class tc " +
        "  WHERE tc.class_id = c.id " +
        "  AND tc.status_id = 3" +
        ")")
    Flux<Class> findBySemesterIdAndNoConfirmedTeacherAssigned(Long semesterId);
}