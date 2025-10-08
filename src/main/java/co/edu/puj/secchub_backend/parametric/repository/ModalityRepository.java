package co.edu.puj.secchub_backend.parametric.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.parametric.model.Modality;

import java.util.Optional;

/**
 * Repository interface for Modality entity.
 * Provides data access operations for modality management in the planning module.
 */
@Repository
public interface ModalityRepository extends JpaRepository<Modality, Long> {
    
    /**
     * Find modality by name.
     * @param name the modality name
     * @return optional modality with the specified name
     */
    Optional<Modality> findByName(String name);
    
    /**
     * Check if modality exists by name.
     * @param name the modality name
     * @return true if modality exists, false otherwise
     */
    boolean existsByName(String name);
}