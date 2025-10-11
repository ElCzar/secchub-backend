package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for DocumentType entity.
 * Provides data access operations for document type management.
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    
    /**
     * Find document type by name.
     * @param name the document type name
     * @return optional document type with the specified name
     */
    Optional<DocumentType> findByName(String name);
    
    /**
     * Check if document type exists by name.
     * @param name the document type name
     * @return true if document type exists, false otherwise
     */
    boolean existsByName(String name);
}