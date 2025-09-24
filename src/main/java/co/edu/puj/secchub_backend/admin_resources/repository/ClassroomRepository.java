package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Classroom entity operations.
 * 
 * <p>This repository provides data access methods for classroom management
 * including CRUD operations, search capabilities, and specialized queries
 * for academic planning and scheduling.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for classroom management</li>
 * <li>Search by name, location, and type</li>
 * <li>Capacity and availability filtering</li>
 * <li>Campus-based organization</li>
 * <li>Pagination support for large datasets</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    /**
     * Find classrooms by room name (partial match, case insensitive).
     * 
     * @param roomName the room name to search for
     * @return list of classrooms matching the criteria
     */
    @Query("SELECT c FROM Classroom c WHERE LOWER(c.room) LIKE LOWER(CONCAT('%', :roomName, '%'))")
    List<Classroom> findByRoomContainingIgnoreCase(@Param("roomName") String roomName);
    
    /**
     * Find classrooms by campus.
     * 
     * @param campus the campus name
     * @return list of classrooms in the specified campus
     */
    List<Classroom> findByCampusIgnoreCase(String campus);
    
    /**
     * Find classrooms by type ID.
     * 
     * @param typeId the classroom type ID
     * @return list of classrooms of the specified type
     */
    List<Classroom> findByClassroomTypeId(Long typeId);
    
    /**
     * Find classrooms with capacity greater than or equal to specified value.
     * 
     * @param minCapacity minimum capacity required
     * @return list of classrooms with sufficient capacity
     */
    List<Classroom> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * Find classrooms by multiple criteria with pagination.
     * 
     * @param campus optional campus filter
     * @param typeId optional classroom type filter
     * @param minCapacity optional minimum capacity filter
     * @param pageable pagination information
     * @return paginated list of classrooms matching the criteria
     */
    @Query("SELECT c FROM Classroom c WHERE " +
           "(:campus IS NULL OR LOWER(c.campus) LIKE LOWER(CONCAT('%', :campus, '%'))) AND " +
           "(:typeId IS NULL OR c.classroomTypeId = :typeId) AND " +
           "(:minCapacity IS NULL OR c.capacity >= :minCapacity)")
    Page<Classroom> findByMultipleCriteria(@Param("campus") String campus,
                                         @Param("typeId") Long typeId,
                                         @Param("minCapacity") Integer minCapacity,
                                         Pageable pageable);
    
    /**
     * Check if a classroom with the same room and location already exists.
     * Used to prevent duplicates.
     * 
     * @param room the room name
     * @param location the location
     * @param id optional ID to exclude from search (for updates)
     * @return true if a duplicate exists, false otherwise
     */
    @Query("SELECT COUNT(c) > 0 FROM Classroom c WHERE " +
           "c.room = :room AND c.location = :location AND " +
           "(:id IS NULL OR c.id != :id)")
    boolean existsByRoomAndLocationAndIdNot(@Param("room") String room,
                                          @Param("location") String location,
                                          @Param("id") Long id);
    
    /**
     * Find available classrooms for a specific time slot.
     * This method finds classrooms that are not occupied during the specified time.
     * 
     * @param day the day of the week
     * @param startTime the start time in HH:mm format
     * @param endTime the end time in HH:mm format
     * @param minCapacity optional minimum capacity requirement
     * @return list of available classrooms
     */
    @Query("SELECT c FROM Classroom c WHERE " +
           "(:minCapacity IS NULL OR c.capacity >= :minCapacity) AND " +
           "c.id NOT IN (" +
           "    SELECT DISTINCT cs.classroomId FROM ClassSchedule cs " +
           "    WHERE cs.day = :day AND " +
           "    ((cs.startTime <= :startTime AND cs.endTime > :startTime) OR " +
           "     (cs.startTime < :endTime AND cs.endTime >= :endTime) OR " +
           "     (cs.startTime >= :startTime AND cs.endTime <= :endTime))" +
           ")")
    List<Classroom> findAvailableClassrooms(@Param("day") String day,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime,
                                          @Param("minCapacity") Integer minCapacity);
    
    /**
     * Find all classrooms ordered by campus and room.
     * 
     * @return list of all classrooms sorted by campus and room
     */
    @Query("SELECT c FROM Classroom c ORDER BY c.campus ASC, c.room ASC")
    List<Classroom> findAllOrderedByCampusAndRoom();
    
    /**
     * Find all classrooms with type information loaded (solving lazy loading).
     * 
     * @return list of all classrooms with type eagerly loaded
     */
    @EntityGraph(attributePaths = {"classroomType"})
    @Query("SELECT c FROM Classroom c ORDER BY c.campus ASC, c.room ASC")
    List<Classroom> findAllWithType();
    
    /**
     * Find classroom by ID with type information loaded.
     * 
     * @param id the classroom ID
     * @return optional classroom with type eagerly loaded
     */
    @EntityGraph(attributePaths = {"classroomType"})
    @Query("SELECT c FROM Classroom c WHERE c.id = :id")
    Optional<Classroom> findByIdWithType(@Param("id") Long id);
}
