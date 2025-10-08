package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Classroom entity.
 * Provides data access operations for classroom management in the planning module.
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    /**
     * Find classrooms by classroom type ID.
     * @param classroomTypeId the classroom type ID
     * @return list of classrooms of the specified type
     */
    List<Classroom> findByClassroomTypeId(Long classroomTypeId);
    
    /**
     * Find classrooms by campus.
     * @param campus the campus name
     * @return list of classrooms in the specified campus
     */
    List<Classroom> findByCampus(String campus);
    
    /**
     * Find classrooms by campus and location.
     * @param campus the campus name
     * @param location the location within the campus
     * @return list of classrooms in the specified campus and location
     */
    List<Classroom> findByCampusAndLocation(String campus, String location);
    
    /**
     * Find classrooms with capacity greater than or equal to the specified value.
     * @param capacity the minimum capacity
     * @return list of classrooms with adequate capacity
     */
    List<Classroom> findByCapacityGreaterThanEqual(Integer capacity);
}