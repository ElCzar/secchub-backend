package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Teacher} entities.
 * 
 * <p>This repository provides comprehensive data access operations for teachers,
 * including workload management, availability calculations, assignment tracking,
 * and analytics. It's essential for the academic planning module to optimize
 * teacher assignments and ensure proper workload distribution.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for teachers</li>
 * <li>Workload tracking and availability calculations</li>
 * <li>Employment type-based filtering and analytics</li>
 * <li>Teacher assignment optimization queries</li>
 * <li>Workload statistics and reporting</li>
 * <li>Teacher capacity and availability management</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * Finds a teacher by their associated user ID.
     * 
     * @param userId the ID of the user associated with the teacher
     * @return optional teacher if found
     */
    Optional<Teacher> findByUserId(Long userId);
    
    /**
     * Finds all teachers with a specific employment type.
     * 
     * @param employmentTypeId the ID of the employment type
     * @return list of teachers with the specified employment type
     */
    List<Teacher> findByEmploymentTypeId(Long employmentTypeId);
    
    /**
     * Finds all full-time teachers.
     * 
     * @param fullTimeEmploymentTypeId the employment type ID for full-time teachers
     * @return list of full-time teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.employmentTypeId = :fullTimeEmploymentTypeId")
    List<Teacher> findFullTimeTeachers(@Param("fullTimeEmploymentTypeId") Long fullTimeEmploymentTypeId);
    
    /**
     * Finds all adjunct (part-time/contract) teachers.
     * 
     * @param adjunctEmploymentTypeId the employment type ID for adjunct teachers
     * @return list of adjunct teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.employmentTypeId = :adjunctEmploymentTypeId")
    List<Teacher> findAdjunctTeachers(@Param("adjunctEmploymentTypeId") Long adjunctEmploymentTypeId);
    
    /**
     * Finds teachers who have available hours for additional assignments.
     * Compares maximum hours with currently assigned hours.
     * 
     * @return list of teachers with available hours
     */
    @Query("SELECT t FROM Teacher t WHERE t.maxHours > " +
           "(SELECT COALESCE(SUM(tc.workHours), 0) FROM TeacherAssignment tc WHERE tc.teacherId = t.id)")
    List<Teacher> findTeachersWithAvailableHours();
    
    /**
     * Finds teachers whose maximum hours fall within a specified range.
     * 
     * @param minHours the minimum number of hours
     * @param maxHours the maximum number of hours
     * @return list of teachers within the hour range
     */
    List<Teacher> findByMaxHoursBetween(Integer minHours, Integer maxHours);
    
    /**
     * Finds teachers with more than the specified maximum hours.
     * 
     * @param hours the hour threshold
     * @return list of teachers with more than the specified hours
     */
    List<Teacher> findByMaxHoursGreaterThan(Integer hours);
    
    /**
     * Finds teachers with less than the specified maximum hours.
     * 
     * @param hours the hour threshold
     * @return list of teachers with fewer than the specified hours
     */
    List<Teacher> findByMaxHoursLessThan(Integer hours);
    
    /**
     * Finds teachers who currently have no class assignments.
     * 
     * @return list of teachers without any assignments
     */
    @Query("SELECT t FROM Teacher t WHERE t.id NOT IN " +
           "(SELECT DISTINCT tc.teacherId FROM TeacherAssignment tc WHERE tc.teacherId IS NOT NULL)")
    List<Teacher> findTeachersWithoutAssignments();
    
    /**
     * Finds teachers who are overloaded (assigned more hours than their maximum).
     * 
     * @return list of overloaded teachers
     */
    @Query("SELECT t FROM Teacher t WHERE t.maxHours < " +
           "(SELECT COALESCE(SUM(tc.workHours), 0) FROM TeacherAssignment tc WHERE tc.teacherId = t.id)")
    List<Teacher> findOverloadedTeachers();
    
    // Obtener profesores con sus horas asignadas
    @Query("SELECT t, COALESCE(SUM(tc.workHours), 0) as assignedHours " +
           "FROM Teacher t LEFT JOIN TeacherAssignment tc ON t.id = tc.teacherId " +
           "GROUP BY t.id")
    List<Object[]> findTeachersWithAssignedHours();
    
    // Buscar profesores disponibles para un número específico de horas
    @Query("SELECT t FROM Teacher t WHERE (t.maxHours - " +
           "(SELECT COALESCE(SUM(tc.workHours), 0) FROM TeacherAssignment tc WHERE tc.teacherId = t.id)) >= :requiredHours")
    List<Teacher> findTeachersAvailableForHours(@Param("requiredHours") Integer requiredHours);
    
    // Buscar profesores por tipo de empleo con paginación
    Page<Teacher> findByEmploymentTypeId(Long employmentTypeId, Pageable pageable);
    
    // Verificar si un usuario ya es profesor
    boolean existsByUserId(Long userId);
    
    // Contar profesores por tipo de empleo
    long countByEmploymentTypeId(Long employmentTypeId);
    
    // Buscar profesores ordenados por horas máximas
    List<Teacher> findAllByOrderByMaxHoursDesc();
    
    List<Teacher> findAllByOrderByMaxHoursAsc();
    
    // Buscar profesores con múltiples criterios
    @Query("SELECT t FROM Teacher t WHERE " +
           "(:employmentTypeId IS NULL OR t.employmentTypeId = :employmentTypeId) AND " +
           "(:minHours IS NULL OR t.maxHours >= :minHours) AND " +
           "(:maxHours IS NULL OR t.maxHours <= :maxHours)")
    Page<Teacher> findByMultipleCriteria(@Param("employmentTypeId") Long employmentTypeId,
                                       @Param("minHours") Integer minHours,
                                       @Param("maxHours") Integer maxHours,
                                       Pageable pageable);
    
    // Obtener estadísticas de carga de trabajo
    @Query("SELECT t.employmentTypeId, COUNT(t), AVG(t.maxHours), " +
           "AVG(COALESCE((SELECT SUM(tc.workHours) FROM TeacherAssignment tc WHERE tc.teacherId = t.id), 0)) " +
           "FROM Teacher t GROUP BY t.employmentTypeId")
    List<Object[]> getWorkloadStatisticsByEmploymentType();
    
    // Buscar profesores con asignaciones activas
    @Query("SELECT DISTINCT t FROM Teacher t " +
           "JOIN TeacherAssignment tc ON t.id = tc.teacherId " +
           "JOIN AcademicClass ac ON tc.classId = ac.id " +
           "WHERE ac.statusId = :activeStatusId")
    List<Teacher> findTeachersWithActiveAssignments(@Param("activeStatusId") Long activeStatusId);
    
    // Buscar profesores por facultad (a través de users) - COMENTADO: No hay relación Users definida
    // @Query("SELECT t FROM Teacher t " +
    //        "JOIN Users u ON t.userId = u.id " +
    //        "WHERE u.faculty = :faculty")
    // List<Teacher> findByFaculty(@Param("faculty") String faculty);
    
    // Buscar profesores activos (a través de users) - COMENTADO: No hay relación Users definida
    // @Query("SELECT t FROM Teacher t " +
    //        "JOIN Users u ON t.userId = u.id " +
    //        "WHERE u.statusId = :activeStatusId")
    // List<Teacher> findActiveTeachers(@Param("activeStatusId") Long activeStatusId);
    
    // Buscar profesores con información de usuario - COMENTADO: No hay relación Users definida
    // @Query("SELECT t FROM Teacher t " +
    //        "LEFT JOIN FETCH Users u ON t.userId = u.id " +
    //        "WHERE t.id = :teacherId")
    // Optional<Teacher> findByIdWithUserInfo(@Param("teacherId") Long teacherId);
    
    // Obtener ranking de profesores por horas asignadas
    @Query("SELECT t, COALESCE(SUM(tc.workHours), 0) as totalHours " +
           "FROM Teacher t LEFT JOIN TeacherAssignment tc ON t.id = tc.teacherId " +
           "GROUP BY t.id ORDER BY totalHours DESC")
    List<Object[]> getTeachersRankedByAssignedHours();
    
    // Buscar profesores que pueden tomar horas extra
    @Query("SELECT t FROM Teacher t WHERE t.maxHours > 0 AND " +
           "(SELECT COALESCE(SUM(tc.workHours), 0) FROM TeacherAssignment tc WHERE tc.teacherId = t.id) < t.maxHours")
    List<Teacher> findTeachersAvailableForExtraHours();
}
