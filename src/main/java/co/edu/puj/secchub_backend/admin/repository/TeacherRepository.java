package co.edu.puj.secchub_backend.admin.repository;

import co.edu.puj.secchub_backend.admin.model.Teacher;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for Teacher entity.
 * Provides data access operations for teacher management in the admin module.
 */
@Repository
public interface TeacherRepository extends R2dbcRepository<Teacher, Long> {
    
    /**
     * Find teacher by user ID.
     * @param userId the user ID associated with the teacher
     * @return mono teacher with the specified user ID
     */
    Mono<Teacher> findByUserId(Long userId);
    
    /**
     * Find teachers by employment type ID.
     * @param employmentTypeId the employment type ID
     * @return flux of teachers with the specified employment type
     */
    Flux<Teacher> findByEmploymentTypeId(Long employmentTypeId);
    
    /**
     * Find teachers with max hours greater than or equal to the specified value.
     * @param maxHours the minimum max hours
     * @return flux of teachers with adequate max hours capacity
     */
    Flux<Teacher> findByMaxHoursGreaterThanEqual(Integer maxHours);
    
    /**
     * Find teachers with max hours less than or equal to the specified value.
     * @param maxHours the maximum max hours
     * @return flux of teachers within the specified max hours limit
     */
    Flux<Teacher> findByMaxHoursLessThanEqual(Integer maxHours);
    
    /**
     * Check if teacher exists by user ID.
     * @param userId the user ID
     * @return true if teacher exists for the user, false otherwise
     */
    Mono<Boolean> existsByUserId(Long userId);
    
    /**
     * Find teachers by employment type and available hours.
     * @param employmentTypeId the employment type ID
     * @param minHours the minimum available hours
     * @return flux of teachers matching the criteria
     */
    @Query("""
        SELECT t FROM Teacher t 
        WHERE t.employmentTypeId = :employmentTypeId 
        AND t.maxHours >= :minHours
    """)
    Flux<Teacher> findByEmploymentTypeAndMinHours(
        @Param("employmentTypeId") Long employmentTypeId, 
        @Param("minHours") Integer minHours
    );
    
    /**
     * Find all teachers with their user information (for reporting purposes).
     * @return flux of teachers ordered by max hours descending
     */
    @Query("SELECT t FROM Teacher t ORDER BY t.maxHours DESC")
    Flux<Teacher> findAllOrderByMaxHoursDesc();
}