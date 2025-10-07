package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository for Status entity.
 * Provides access to status records for tracking application states.
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    
    /**
     * Finds a status by its name.
     * @param name the status name
     * @return Optional containing the status if found
     */
    Optional<Status> findByName(String name);
    
    /**
     * Checks if a status exists by name.
     * @param name the status name
     * @return true if status exists, false otherwise
     */
    boolean existsByName(String name);
}