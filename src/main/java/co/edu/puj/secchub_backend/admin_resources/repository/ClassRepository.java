package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.AcademicClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing {@link AcademicClass} entities.
 * 
 * <p>This repository provides comprehensive data access operations for academic classes,
 * including CRUD operations, custom queries for filtering, searching, and analytics.
 * It supports both simple queries using Spring Data JPA method naming conventions
 * and complex custom queries using JPQL.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations (inherited from JpaRepository)</li>
 * <li>Filtering by course, semester, and status</li>
 * <li>Date range queries for scheduling conflicts</li>
 * <li>Capacity and availability calculations</li>
 * <li>Pagination support for large datasets</li>
 * <li>Multi-criteria search capabilities</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClassRepository extends JpaRepository<AcademicClass, Long> {
    
    /**
     * Finds all academic classes for a specific course.
     * 
     * @param courseId the ID of the course to search for
     * @return list of academic classes belonging to the specified course
     */
    List<AcademicClass> findByCourseId(Long courseId);
    
    /**
     * Finds all academic classes for a specific semester.
     * 
     * @param semesterId the ID of the semester to search for
     * @return list of academic classes belonging to the specified semester
     */
    List<AcademicClass> findBySemesterId(Long semesterId);
    
    /**
     * Finds all academic classes with a specific status.
     * 
     * @param statusId the ID of the status to search for
     * @return list of academic classes with the specified status
     */
    List<AcademicClass> findByStatusId(Long statusId);
    
    /**
     * Finds academic classes that match both course and semester criteria.
     * 
     * @param courseId the ID of the course
     * @param semesterId the ID of the semester
     * @return list of academic classes matching both criteria
     */
    List<AcademicClass> findByCourseIdAndSemesterId(Long courseId, Long semesterId);
    
    /**
     * Finds all active academic classes by excluding those with inactive status.
     * 
     * @param inactiveStatusId the status ID representing inactive classes
     * @return list of active academic classes
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE ac.statusId != :inactiveStatusId")
    List<AcademicClass> findActiveClasses(@Param("inactiveStatusId") Long inactiveStatusId);
    
    /**
     * Finds academic classes within a specific date range.
     * Both start and end dates of the class must fall within the specified range.
     * 
     * @param startDate the earliest start date to include
     * @param endDate the latest end date to include
     * @return list of academic classes within the date range
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE ac.startDate >= :startDate AND ac.endDate <= :endDate")
    List<AcademicClass> findByDateRange(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Finds academic classes that overlap with a specified date range.
     * Useful for detecting scheduling conflicts.
     * 
     * @param startDate the start of the date range to check
     * @param endDate the end of the date range to check
     * @return list of academic classes that overlap with the specified range
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE ac.startDate <= :endDate AND ac.endDate >= :startDate")
    List<AcademicClass> findOverlappingClasses(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * Finds academic classes that still have available capacity for enrollment.
     * Compares the class capacity with the count of current teacher assignments.
     * 
     * @return list of academic classes with available capacity
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE ac.capacity > " +
           "(SELECT COUNT(ta) FROM TeacherAssignment ta WHERE ta.classId = ac.id)")
    List<AcademicClass> findClassesWithAvailableCapacity();
    
    /**
     * Finds academic classes for a specific semester with pagination support.
     * 
     * @param semesterId the ID of the semester
     * @param pageable pagination information
     * @return paginated list of academic classes for the semester
     */
    Page<AcademicClass> findBySemesterId(Long semesterId, Pageable pageable);
    
    /**
     * Finds academic classes using multiple optional criteria.
     * All parameters are optional (can be null) to create flexible search queries.
     * 
     * @param courseId optional course ID filter
     * @param semesterId optional semester ID filter  
     * @param statusId optional status ID filter
     * @param startDate optional minimum start date filter
     * @param endDate optional maximum end date filter
     * @param pageable pagination information
     * @return paginated list of academic classes matching the criteria
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE " +
           "(:courseId IS NULL OR ac.courseId = :courseId) AND " +
           "(:semesterId IS NULL OR ac.semesterId = :semesterId) AND " +
           "(:statusId IS NULL OR ac.statusId = :statusId) AND " +
           "(:startDate IS NULL OR ac.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR ac.endDate <= :endDate)")
    Page<AcademicClass> findByMultipleCriteria(@Param("courseId") Long courseId,
                                             @Param("semesterId") Long semesterId,
                                             @Param("statusId") Long statusId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             Pageable pageable);
    
    /**
     * Counts the number of academic classes for a specific course.
     * 
     * @param courseId the ID of the course
     * @return count of academic classes for the course
     */
    long countByCourseId(Long courseId);
    
    /**
     * Counts the number of academic classes for a specific semester.
     * 
     * @param semesterId the ID of the semester
     * @return count of academic classes for the semester
     */
    long countBySemesterId(Long semesterId);
    
    /**
     * Checks if an academic class exists with the specified course, semester, and status.
     * 
     * @param courseId the ID of the course
     * @param semesterId the ID of the semester
     * @param statusId the ID of the status
     * @return true if such a class exists, false otherwise
     */
    boolean existsByCourseIdAndSemesterIdAndStatusId(Long courseId, Long semesterId, Long statusId);
    
    /**
     * Finds academic classes that end before the specified date.
     * 
     * @param date the reference date
     * @return list of academic classes ending before the date
     */
    List<AcademicClass> findByEndDateBefore(LocalDate date);
    
    /**
     * Finds academic classes that start after the specified date.
     * 
     * @param date the reference date
     * @return list of academic classes starting after the date
     */
    List<AcademicClass> findByStartDateAfter(LocalDate date);
    
    /**
     * Finds currently active academic classes.
     * Classes are considered current if today's date falls between their start and end dates.
     * 
     * @return list of currently active academic classes
     */
    @Query("SELECT ac FROM AcademicClass ac WHERE ac.startDate <= CURRENT_DATE AND ac.endDate >= CURRENT_DATE")
    List<AcademicClass> findCurrentClasses();
    
    // Future enhancement: Query for fetching classes with related entities
    // TODO: Implement when JPA relationships are added to AcademicClass entity
    // /**
    //  * Finds an academic class by ID with all related schedules and teacher assignments.
    //  * Uses fetch joins to load related entities in a single query.
    //  * 
    //  * @param classId the ID of the academic class
    //  * @return optional academic class with loaded relationships
    //  */
    // @Query("SELECT ac FROM AcademicClass ac " +
    //        "LEFT JOIN FETCH ac.schedules s " +
    //        "LEFT JOIN FETCH ac.teacherAssignments ta " +
    //        "WHERE ac.id = :classId")
    // Optional<AcademicClass> findByIdWithDetails(@Param("classId") Long classId);
}
