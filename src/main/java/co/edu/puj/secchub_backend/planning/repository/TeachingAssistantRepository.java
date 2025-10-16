package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.TeachingAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TeachingAssistant entity.
 * Provides data access operations for teaching assistant management in the planning module.
 */
@Repository
public interface TeachingAssistantRepository extends JpaRepository<TeachingAssistant, Long> {
    
    /**
     * Find teaching assistants by class ID.
     * @param classId the class ID
     * @return list of teaching assistants for the specified class
     */
    List<TeachingAssistant> findByClassId(Long classId);
    
    /**
     * Find teaching assistants by student application ID.
     * @param studentApplicationId the student application ID
     * @return list of teaching assistant assignments for the specified student application
     */
    List<TeachingAssistant> findByStudentApplicationId(Long studentApplicationId);
    
    /**
     * Find teaching assistants with weekly hours greater than or equal to the specified value.
     * @param weeklyHours the minimum weekly hours
     * @return list of teaching assistants with adequate weekly hours
     */
    List<TeachingAssistant> findByWeeklyHoursGreaterThanEqual(Long weeklyHours);
    
    /**
     * Calculate total hours for a student application across all assignments.
     * @param studentApplicationId the student application ID
     * @return total hours assigned to the student
     */
    @Query("""
        SELECT COALESCE(SUM(ta.totalHours), 0) 
        FROM TeachingAssistant ta 
        WHERE ta.studentApplicationId = :studentApplicationId
    """)
    Long calculateTotalHoursByStudentApplication(@Param("studentApplicationId") Long studentApplicationId);
    
    /**
     * Find teaching assistants by class and check if they exceed maximum hours.
     * @param classId the class ID
     * @param maxHours the maximum allowed hours
     * @return list of teaching assistants within the hour limit
     */
    @Query("""
        SELECT ta FROM TeachingAssistant ta 
        WHERE ta.classId = :classId 
        AND ta.totalHours <= :maxHours
    """)
    List<TeachingAssistant> findByClassIdAndTotalHoursLessThanEqual(
        @Param("classId") Long classId, 
        @Param("maxHours") Long maxHours
    );
    
    /**
     * Find teaching assistants for the current semester through class relationship.
     * @param semesterId the semester ID
     * @return list of teaching assistants for the specified semester
     */
    @Query("""
        SELECT ta FROM TeachingAssistant ta 
        JOIN Class c ON ta.classId = c.id 
        WHERE c.semesterId = :semesterId
    """)
    List<TeachingAssistant> findByCurrentSemester(@Param("semesterId") Long semesterId);
}