package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.ClassSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for managing {@link ClassSchedule} entities.
 * 
 * <p>This repository provides comprehensive data access operations for class schedules,
 * including time slot management, conflict detection, classroom availability queries,
 * and schedule optimization. It's essential for the academic planning module to prevent
 * scheduling conflicts and optimize resource utilization.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for schedules</li>
 * <li>Conflict detection for overlapping time slots</li>
 * <li>Classroom and time availability queries</li>
 * <li>Schedule filtering by day, time, modality, and accessibility</li>
 * <li>Complex scheduling analytics and reporting</li>
 * <li>Bulk operations for schedule management</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    
    /**
     * Finds all schedules for a specific academic class.
     * 
     * @param classId the ID of the academic class
     * @return list of schedules for the specified class
     */
    List<ClassSchedule> findByClassId(Long classId);
    
    /**
     * Finds all schedules assigned to a specific classroom.
     * 
     * @param classroomId the ID of the classroom
     * @return list of schedules in the specified classroom
     */
    List<ClassSchedule> findByClassroomId(Long classroomId);
    
    /**
     * Finds all schedules for a specific day of the week.
     * 
     * @param day the day of the week (e.g., "Monday", "Tuesday")
     * @return list of schedules for the specified day
     */
    List<ClassSchedule> findByDay(String day);
    
    /**
     * Finds all schedules with a specific modality.
     * 
     * @param modalityId the ID of the modality (in-person, online, hybrid)
     * @return list of schedules with the specified modality
     */
    List<ClassSchedule> findByModalityId(Long modalityId);
    
    /**
     * Finds all schedules that have disability accessibility accommodations.
     * 
     * @return list of schedules with disability accommodations
     */
    List<ClassSchedule> findByDisabilityTrue();
    
    /**
     * Finds all schedules that do not have disability accessibility accommodations.
     * 
     * @return list of schedules without disability accommodations
     */
    List<ClassSchedule> findByDisabilityFalse();
    
    /**
     * Finds schedules that conflict with a proposed time slot in a specific classroom.
     * This is crucial for preventing double-booking of classrooms.
     * 
     * @param classroomId the ID of the classroom to check
     * @param day the day of the week
     * @param startTime the proposed start time
     * @param endTime the proposed end time
     * @param excludeClassId class ID to exclude from conflict check (for updates)
     * @return list of conflicting schedules
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.classroomId = :classroomId " +
           "AND cs.day = :day " +
           "AND cs.classId != :excludeClassId " +
           "AND ((cs.startTime <= :endTime AND cs.endTime >= :startTime))")
    List<ClassSchedule> findConflictingSchedules(@Param("classroomId") Long classroomId,
                                                @Param("day") String day,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime,
                                                @Param("excludeClassId") Long excludeClassId);
    
    /**
     * Finds available time slots in a classroom by identifying non-conflicting schedules.
     * 
     * @param classroomId the ID of the classroom
     * @param day the day of the week
     * @param startTime the desired start time
     * @param endTime the desired end time
     * @return list of non-conflicting schedules (available time slots)
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.classroomId = :classroomId " +
           "AND cs.day = :day " +
           "AND NOT ((cs.startTime <= :endTime AND cs.endTime >= :startTime))")
    List<ClassSchedule> findAvailableTimeSlots(@Param("classroomId") Long classroomId,
                                             @Param("day") String day,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime);
    
    /**
     * Finds schedules within a specific time range across all days and classrooms.
     * 
     * @param startTime the earliest start time to include
     * @param endTime the latest end time to include
     * @return list of schedules within the time range
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.startTime >= :startTime AND cs.endTime <= :endTime")
    List<ClassSchedule> findByTimeRange(@Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
    
    /**
     * Finds schedules that start at a specific time.
     * 
     * @param startTime the start time to search for
     * @return list of schedules starting at the specified time
     */
    List<ClassSchedule> findByStartTime(LocalTime startTime);
    
    /**
     * Finds schedules that end at a specific time.
     * 
     * @param endTime the end time to search for
     * @return list of schedules ending at the specified time
     */
    List<ClassSchedule> findByEndTime(LocalTime endTime);
    
    /**
     * Finds schedules for a specific day and classroom combination.
     * 
     * @param day the day of the week
     * @param classroomId the ID of the classroom
     * @return list of schedules for the day and classroom
     */
    List<ClassSchedule> findByDayAndClassroomId(String day, Long classroomId);
    
    /**
     * Finds schedules for a specific day and modality combination.
     * 
     * @param day the day of the week
     * @param modalityId the ID of the modality
     * @return list of schedules for the day and modality
     */
    List<ClassSchedule> findByDayAndModalityId(String day, Long modalityId);
    
    /**
     * Finds schedules for a class ordered by day of week and start time.
     * Provides a natural weekly schedule view.
     * 
     * @param classId the ID of the academic class
     * @return list of schedules ordered by day and time
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.classId = :classId " +
           "ORDER BY " +
           "CASE cs.day " +
           "WHEN 'LUNES' THEN 1 " +
           "WHEN 'MARTES' THEN 2 " +
           "WHEN 'MIÉRCOLES' THEN 3 " +
           "WHEN 'JUEVES' THEN 4 " +
           "WHEN 'VIERNES' THEN 5 " +
           "WHEN 'SÁBADO' THEN 6 " +
           "WHEN 'DOMINGO' THEN 7 " +
           "ELSE 8 END, cs.startTime")
    List<ClassSchedule> findByClassIdOrderedByDayAndTime(@Param("classId") Long classId);
    
    /**
     * Finds morning schedules (starting before 12:00 PM).
     * 
     * @return list of morning schedules
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE HOUR(cs.startTime) < 12")
    List<ClassSchedule> findMorningSchedules();
    
    /**
     * Finds afternoon schedules (starting between 12:00 PM and 6:00 PM).
     * 
     * @return list of afternoon schedules
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE HOUR(cs.startTime) >= 12 AND HOUR(cs.startTime) < 18")
    List<ClassSchedule> findAfternoonSchedules();
    
    /**
     * Finds evening schedules (starting after 6:00 PM).
     * 
     * @return list of evening schedules
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE HOUR(cs.startTime) >= 18")
    List<ClassSchedule> findEveningSchedules();
    
    /**
     * Counts the number of schedules in a specific classroom.
     * 
     * @param classroomId the ID of the classroom
     * @return count of schedules in the classroom
     */
    long countByClassroomId(Long classroomId);
    
    /**
     * Counts the number of schedules on a specific day.
     * 
     * @param day the day of the week
     * @return count of schedules on the day
     */
    long countByDay(String day);
    
    /**
     * Checks if there exists a scheduling conflict for the given parameters.
     * Returns true if a conflict exists, false otherwise.
     * 
     * @param classroomId the ID of the classroom to check
     * @param day the day of the week
     * @param startTime the proposed start time
     * @param endTime the proposed end time
     * @param excludeClassId class ID to exclude from conflict check
     * @return true if conflict exists, false otherwise
     */
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs WHERE cs.classroomId = :classroomId " +
           "AND cs.day = :day " +
           "AND cs.classId != :excludeClassId " +
           "AND ((cs.startTime <= :endTime AND cs.endTime >= :startTime))")
    boolean existsConflictingSchedule(@Param("classroomId") Long classroomId,
                                    @Param("day") String day,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime,
                                    @Param("excludeClassId") Long excludeClassId);
    
    /**
     * Finds schedules using multiple optional criteria with pagination support.
     * All parameters are optional to create flexible search capabilities.
     * 
     * @param classId optional class ID filter
     * @param classroomId optional classroom ID filter
     * @param day optional day filter
     * @param modalityId optional modality ID filter
     * @param disability optional disability accommodation filter
     * @param pageable pagination information
     * @return paginated list of schedules matching the criteria
     */
    @Query("SELECT cs FROM ClassSchedule cs WHERE " +
           "(:classId IS NULL OR cs.classId = :classId) AND " +
           "(:classroomId IS NULL OR cs.classroomId = :classroomId) AND " +
           "(:day IS NULL OR cs.day = :day) AND " +
           "(:modalityId IS NULL OR cs.modalityId = :modalityId) AND " +
           "(:disability IS NULL OR cs.disability = :disability)")
    Page<ClassSchedule> findByMultipleCriteria(@Param("classId") Long classId,
                                             @Param("classroomId") Long classroomId,
                                             @Param("day") String day,
                                             @Param("modalityId") Long modalityId,
                                             @Param("disability") Boolean disability,
                                             Pageable pageable);
    
    /**
     * Finds schedules for a specific day ordered by start time.
     * 
     * @param day the day of the week
     * @return list of schedules for the day ordered by start time
     */
    List<ClassSchedule> findByDayOrderByStartTime(String day);
    
    /**
     * Deletes all schedules associated with a specific class.
     * Useful when removing or rescheduling an entire class.
     * 
     * @param classId the ID of the class whose schedules should be deleted
     */
    void deleteByClassId(Long classId);
    
    /**
     * Finds unique combinations of classroom and day for occupancy analysis.
     * Returns raw data that can be used to generate classroom utilization reports.
     * 
     * @return list of object arrays containing [classroomId, day] combinations
     */
    @Query("SELECT DISTINCT cs.classroomId, cs.day FROM ClassSchedule cs ORDER BY cs.classroomId, cs.day")
    List<Object[]> findDistinctClassroomAndDayCombinations();
}
