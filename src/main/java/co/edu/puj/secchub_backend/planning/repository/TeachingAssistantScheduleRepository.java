package co.edu.puj.secchub_backend.planning.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleWithDetailsDTO;
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

    /**
     * Find teaching assistant schedules that have conflicts for the given semester.
     * Conflicts occur when the same user (via student application) has multiple TA assignments
     * with overlapping schedules on the same day.
     * @param semesterId the semester ID
     * @return flux of conflicting teaching assistant schedules
     */
    @Query("""
        SELECT DISTINCT
            tas1.id as schedule_id,
            tas1.teaching_assistant_id,
            ta1.student_application_id,
            sa1.user_id,
            ta1.class_id,
            sa1.section_id,
            tas1.day,
            tas1.start_time,
            tas1.end_time
        FROM teaching_assistant_schedule tas1
        INNER JOIN teaching_assistant ta1 ON tas1.teaching_assistant_id = ta1.id
        INNER JOIN student_application sa1 ON ta1.student_application_id = sa1.id
        INNER JOIN class c1 ON ta1.class_id = c1.id
        INNER JOIN teaching_assistant_schedule tas2 ON tas1.day = tas2.day
            AND tas1.id != tas2.id  -- Changed from tas1.id < tas2.id
            AND tas1.start_time < tas2.end_time
            AND tas1.end_time > tas2.start_time
        INNER JOIN teaching_assistant ta2 ON tas2.teaching_assistant_id = ta2.id
        INNER JOIN student_application sa2 ON ta2.student_application_id = sa2.id
        INNER JOIN class c2 ON ta2.class_id = c2.id
        WHERE c1.semester_id = :semesterId
        AND c2.semester_id = :semesterId
        AND sa1.user_id = sa2.user_id
        AND sa1.status_id = 8
        AND sa2.status_id = 8
        """)
    Flux<TeachingAssistantScheduleWithDetailsDTO> findConflictingSchedulesWithDetailsBySemesterId(
        @Param("semesterId") Long semesterId
    );
}
