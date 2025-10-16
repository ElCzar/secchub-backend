package co.edu.puj.secchub_backend.planning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.planning.model.TeachingAssistantSchedule;

import java.util.List;

/**
 * Repository interface for TeachingAssistantSchedule entity.
 * Provides data access operations for teaching assistant schedule management.
 */
@Repository
public interface TeachingAssistantScheduleRepository extends JpaRepository<TeachingAssistantSchedule, Long> {
    
    /**
     * Find schedules by teaching assistant ID.
     * @param teachingAssistantId the teaching assistant ID
     * @return list of schedules for the specified teaching assistant
     */
    List<TeachingAssistantSchedule> findByTeachingAssistantId(Long teachingAssistantId);
}
