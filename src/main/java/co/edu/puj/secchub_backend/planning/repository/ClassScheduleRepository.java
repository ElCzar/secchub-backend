package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.ClassSchedule;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

/**
 * Repository interface for ClassSchedule entity.
 * Provides data access operations for class schedule management in the planning module.
 */
@Repository
public interface ClassScheduleRepository extends R2dbcRepository<ClassSchedule, Long> {
    
    /**
     * Find schedules by class ID.
     * @param classId the class ID
     * @return flux of schedules for the specified class
     */
    Flux<ClassSchedule> findByClassId(Long classId);
    
    /**
     * Find schedules by classroom ID.
     * @param classroomId the classroom ID
     * @return flux of schedules for the specified classroom
     */
    Flux<ClassSchedule> findByClassroomId(Long classroomId);
    
    /**
     * Find schedules by day.
     * @param day the day of the week
     * @return flux of schedules for the specified day
     */
    Flux<ClassSchedule> findByDay(String day);
    
    /**
     * Find schedules by modality ID.
     * @param modalityId the modality ID
     * @return flux of schedules for the specified modality
     */
    Flux<ClassSchedule> findByModalityId(Long modalityId);

    /**
     * Find schedules with disability accommodations.
     * @param disability true to find schedules with disability accommodations
     * @return flux of schedules with disability considerations
     */
    Flux<ClassSchedule> findByDisability(Boolean disability);

    /**
     * Find conflicting schedules for a classroom on a specific day and time range.
     * @param classroomId the classroom ID
     * @param day the day of the week
     * @param startTime the start time
     * @param endTime the end time
     * @return flux of conflicting schedules
     */
    @Query("""
        SELECT cs FROM ClassSchedule cs 
        WHERE cs.classroomId = :classroomId 
        AND cs.day = :day 
        AND ((cs.startTime < :endTime AND cs.endTime > :startTime))
    """)
    Flux<ClassSchedule> findConflictingSchedules(
        @Param("classroomId") Long classroomId,
        @Param("day") String day,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}