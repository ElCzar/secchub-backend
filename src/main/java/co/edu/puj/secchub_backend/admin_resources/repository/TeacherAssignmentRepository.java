package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link TeacherAssignment} entities.
 * 
 * <p>This repository provides comprehensive data access operations for teacher assignments,
 * which represent the relationship between teachers and academic classes. It manages
 * the assignment process, including workload tracking, status management, and
 * teacher response handling.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for teacher assignments</li>
 * <li>Assignment status tracking (pending, accepted, rejected)</li>
 * <li>Workload calculations and hour management</li>
 * <li>Teacher availability and capacity queries</li>
 * <li>Assignment conflict detection and resolution</li>
 * <li>Bulk operations for assignment management</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    /**
     * Finds all assignments for a specific teacher.
     * 
     * @param teacherId the ID of the teacher
     * @return list of assignments for the teacher
     */
    List<TeacherAssignment> findByTeacherId(Long teacherId);

    /**
     * Finds all assignments for a specific academic class.
     * 
     * @param classId the ID of the academic class
     * @return list of assignments for the class
     */
    List<TeacherAssignment> findByClassId(Long classId);

    /**
     * Finds assignments for a teacher with a specific status.
     * 
     * @param teacherId the ID of the teacher
     * @param statusId the ID of the assignment status
     * @return list of assignments matching teacher and status
     */
    List<TeacherAssignment> findByTeacherIdAndStatusId(Long teacherId, Long statusId);

    /**
     * Finds a specific assignment between a teacher and a class.
     * 
     * @param teacherId the ID of the teacher
     * @param classId the ID of the academic class
     * @return optional assignment if found
     */
    Optional<TeacherAssignment> findByTeacherIdAndClassId(Long teacherId, Long classId);
        /**
         * Finds a specific assignment for a class and teacher.
         *
         * @param classId the ID of the academic class
         * @param teacherId the ID of the teacher
         * @return optional assignment if found
         */
        Optional<TeacherAssignment> findByClassIdAndTeacherId(Long classId, Long teacherId);

    /**
     * Finds all assignments with a specific status.
     * 
     * @param statusId the ID of the assignment status
     * @return list of assignments with the specified status
     */
    List<TeacherAssignment> findByStatusId(Long statusId);

    /**
     * Checks if a teacher is assigned to a specific class.
     * 
     * @param teacherId the ID of the teacher
     * @param classId the ID of the academic class
     * @return true if assignment exists, false otherwise
     */
    boolean existsByTeacherIdAndClassId(Long teacherId, Long classId);

    /**
     * Counts the total number of assignments for a teacher.
     * 
     * @param teacherId the ID of the teacher
     * @return count of assignments for the teacher
     */
    long countByTeacherId(Long teacherId);

    /**
     * Counts the total number of assignments for a specific class.
     * 
     * @param classId the ID of the academic class
     * @return count of assignments for the class
     */
    long countByClassId(Long classId);

    /**
     * Calculates the total work hours assigned to a teacher.
     * 
     * @param teacherId the ID of the teacher
     * @return total work hours assigned to the teacher
     */
    @Query("SELECT COALESCE(SUM(ta.workHours), 0) FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId")
    Integer getTotalWorkHoursByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Calcula los minutos totales planificados para un profesor basado en los horarios
     * de las clases a las que está asignado (usa tablas reales: teacher_class, class, class_schedule).
     * Se considera únicamente asignaciones aceptadas (status_id = 2) para reflejar horas realmente dictadas.
     */
    @Query(value = "SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, cs.start_time, cs.end_time)), 0) " +
                   "FROM teacher_class tc " +
                   "JOIN `class` c ON tc.class_id = c.id " +
                   "JOIN class_schedule cs ON c.id = cs.class_id " +
                   "WHERE tc.teacher_id = :teacherId AND tc.status_id = 2", nativeQuery = true)
    Integer getTotalScheduledMinutesByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Finds all accepted assignments for a teacher.
     * Status ID 2 typically represents accepted assignments.
     * 
     * @param teacherId the ID of the teacher
     * @return list of accepted assignments for the teacher
     */
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId AND ta.statusId = 2")
    List<TeacherAssignment> findAcceptedAssignmentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Finds all pending assignments for a teacher.
     * Status ID 1 typically represents pending assignments.
     * 
     * @param teacherId the ID of the teacher
     * @return list of pending assignments for the teacher
     */
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId AND ta.statusId = 1")
    List<TeacherAssignment> findPendingAssignmentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Finds all rejected assignments for a teacher.
     * Status ID 3 typically represents rejected assignments.
     * 
     * @param teacherId the ID of the teacher
     * @return list of rejected assignments for the teacher
     */
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId AND ta.statusId = 3")
    List<TeacherAssignment> findRejectedAssignmentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Deletes all assignments for a specific class.
     * Useful when removing or canceling an academic class.
     * 
     * @param classId the ID of the academic class
     */
    void deleteByClassId(Long classId);

    /**
     * Deletes all assignments for a specific teacher.
     * Useful when removing a teacher from the system.
     * 
     * @param teacherId the ID of the teacher
     */
    void deleteByTeacherId(Long teacherId);
}
