package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for EmploymentType entity.
 * Provides data access operations for employment type management.
 */
@Repository
public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    
    /**
     * Find employment type by name.
     * @param name the employment type name
     * @return optional employment type with the specified name
     */
    Optional<EmploymentType> findByName(String name);
    
    /**
     * Check if employment type exists by name.
     * @param name the employment type name
     * @return true if employment type exists, false otherwise
     */
    boolean existsByName(String name);
}