package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.Status;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository for Status entity.
 * Provides access to status records for tracking application states.
 */
@Repository
public interface StatusRepository extends R2dbcRepository<Status, Long> {
    
    /**
     * Finds a status by its name.
     * @param name the status name
     * @return Mono containing the status if found
     */
    Mono<Status> findByName(String name);
    
    /**
     * Checks if a status exists by name.
     * @param name the status name
     * @return true if status exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);
}