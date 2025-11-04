package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.TeachingAssistant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for TeachingAssistant entity.
 * Provides data access operations for teaching assistant management in the planning module.
 */
@Repository
public interface TeachingAssistantRepository extends R2dbcRepository<TeachingAssistant, Long> {
    
    /**
     * Find teaching assistants by class ID.
     * @param classId the class ID
     * @return flux of teaching assistants for the specified class
     */
    Flux<TeachingAssistant> findByClassId(Long classId);
    
    /**
     * Find teaching assistants by student application ID.
     * @param studentApplicationId the student application ID
     * @return mono of teaching assistant assignments for the specified student application
     */
    Mono<TeachingAssistant> findByStudentApplicationId(Long studentApplicationId);
    
    /**
     * Find teaching assistants with weekly hours greater than or equal to the specified value.
     * @param weeklyHours the minimum weekly hours
     * @return flux of teaching assistants with adequate weekly hours
     */
    Flux<TeachingAssistant> findByWeeklyHoursGreaterThanEqual(Long weeklyHours);
}