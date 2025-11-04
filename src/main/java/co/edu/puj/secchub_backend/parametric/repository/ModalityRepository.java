package co.edu.puj.secchub_backend.parametric.repository;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.parametric.model.Modality;
import reactor.core.publisher.Mono;


/**
 * Repository interface for Modality entity.
 * Provides data access operations for modality management in the planning module.
 */
@Repository
public interface ModalityRepository extends R2dbcRepository<Modality, Long> {
    
    /**
     * Find modality by name.
     * @param name the modality name
     * @return optional modality with the specified name
     */
    Mono<Modality> findByName(String name);
    
    /**
     * Check if modality exists by name.
     * @param name the modality name
     * @return true if modality exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);
}