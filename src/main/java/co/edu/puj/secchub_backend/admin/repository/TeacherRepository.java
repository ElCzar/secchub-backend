package co.edu.puj.secchub_backend.admin.repository;

import co.edu.puj.secchub_backend.admin.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Teacher entity.
 * Provides data access operations for teacher management in the admin module.
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * Find teacher by user ID.
     * @param userId the user ID associated with the teacher
     * @return optional teacher with the specified user ID
     */
    Optional<Teacher> findByUserId(Long userId);
    
    /**
     * Find teachers by employment type ID.
     * @param employmentTypeId the employment type ID
     * @return list of teachers with the specified employment type
     */
    List<Teacher> findByEmploymentTypeId(Long employmentTypeId);
    
    /**
     * Find teachers with max hours greater than or equal to the specified value.
     * @param maxHours the minimum max hours
     * @return list of teachers with adequate max hours capacity
     */
    List<Teacher> findByMaxHoursGreaterThanEqual(Integer maxHours);
    
    /**
     * Find teachers with max hours less than or equal to the specified value.
     * @param maxHours the maximum max hours
     * @return list of teachers within the specified max hours limit
     */
    List<Teacher> findByMaxHoursLessThanEqual(Integer maxHours);
    
    /**
     * Check if teacher exists by user ID.
     * @param userId the user ID
     * @return true if teacher exists for the user, false otherwise
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find teachers by employment type and available hours.
     * @param employmentTypeId the employment type ID
     * @param minHours the minimum available hours
     * @return list of teachers matching the criteria
     */
    @Query("""
        SELECT t FROM Teacher t 
        WHERE t.employmentTypeId = :employmentTypeId 
        AND t.maxHours >= :minHours
    """)
    List<Teacher> findByEmploymentTypeAndMinHours(
        @Param("employmentTypeId") Long employmentTypeId, 
        @Param("minHours") Integer minHours
    );
    
    /**
     * Find all teachers with their user information (for reporting purposes).
     * @return list of teachers ordered by max hours descending
     */
    @Query("SELECT t FROM Teacher t ORDER BY t.maxHours DESC")
    List<Teacher> findAllOrderByMaxHoursDesc();
}