package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.DocumentType;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DocumentType entity.
 * Provides data access operations for document type management.
 */
@Repository
public interface DocumentTypeRepository extends R2dbcRepository<DocumentType, Long> {
    
    /**
     * Find document type by name.
     * @param name the document type name
     * @return mono document type with the specified name
     */
    Mono<DocumentType> findByName(String name);
    
    /**
     * Check if document type exists by name.
     * @param name the document type name
     * @return true if document type exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);
}