package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.EmploymentType;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for EmploymentType entity.
 * Provides data access operations for employment type management.
 */
@Repository
public interface EmploymentTypeRepository extends R2dbcRepository<EmploymentType, Long> {
    
    /**
     * Find employment type by name.
     * @param name the employment type name
     * @return optional employment type with the specified name
     */
    Mono<EmploymentType> findByName(String name);
    
    /**
     * Check if employment type exists by name.
     * @param name the employment type name
     * @return true if employment type exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);
}