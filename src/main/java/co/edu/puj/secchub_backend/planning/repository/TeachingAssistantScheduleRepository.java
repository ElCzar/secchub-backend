package co.edu.puj.secchub_backend.planning.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.planning.model.TeachingAssistantSchedule;
import reactor.core.publisher.Flux;

/**
 * Repository interface for TeachingAssistantSchedule entity.
 * Provides data access operations for teaching assistant schedule management.
 */
@Repository
public interface TeachingAssistantScheduleRepository extends R2dbcRepository<TeachingAssistantSchedule, Long> {

    /**
     * Find schedules by teaching assistant ID.
     * @param teachingAssistantId the teaching assistant ID
     * @return flux of schedules for the specified teaching assistant
     */
    Flux<TeachingAssistantSchedule> findByTeachingAssistantId(Long teachingAssistantId);
}
