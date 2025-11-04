package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.Classroom;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Classroom entity.
 * Provides data access operations for classroom management in the planning module.
 */
@Repository
public interface ClassroomRepository extends R2dbcRepository<Classroom, Long> {
    
    /**
     * Find classrooms by classroom type ID.
     * @param classroomTypeId the classroom type ID
     * @return flux of classrooms of the specified type
     */
    Flux<Classroom> findByClassroomTypeId(Long classroomTypeId);
    
    /**
     * Find classrooms by campus.
     * @param campus the campus name
     * @return flux of classrooms in the specified campus
     */
    Flux<Classroom> findByCampus(String campus);
    
    /**
     * Find classrooms by campus and location.
     * @param campus the campus name
     * @param location the location within the campus
     * @return flux of classrooms in the specified campus and location
     */
    Flux<Classroom> findByCampusAndLocation(String campus, String location);
    
    /**
     * Find classrooms with capacity greater than or equal to the specified value.
     * @param capacity the minimum capacity
     * @return flux of classrooms with adequate capacity
     */
    Flux<Classroom> findByCapacityGreaterThanEqual(Integer capacity);
}