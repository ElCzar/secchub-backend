package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.Modality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Modality entity operations.
 * 
 * <p>This repository provides data access methods for modality management
 * including CRUD operations and specialized queries for academic planning.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for modality management</li>
 * <li>Search by name</li>
 * <li>Ordered retrieval for UI display</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ModalityRepository extends JpaRepository<Modality, Long> {
    
    /**
     * Find modality by name (case insensitive).
     * 
     * @param name the modality name
     * @return optional modality if found
     */
    Optional<Modality> findByNameIgnoreCase(String name);
    
    /**
     * Check if a modality with the given name exists.
     * 
     * @param name the modality name
     * @return true if exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find all modalities ordered by name.
     * 
     * @return list of all modalities sorted by name
     */
    @Query("SELECT m FROM Modality m ORDER BY m.name ASC")
    List<Modality> findAllOrderedByName();
}
