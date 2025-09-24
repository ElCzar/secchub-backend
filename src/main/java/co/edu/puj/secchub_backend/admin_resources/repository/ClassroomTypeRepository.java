package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.ClassroomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ClassroomType entity operations.
 * 
 * <p>This repository provides data access methods for classroom type management
 * including CRUD operations and specialized queries for academic planning.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Basic CRUD operations for classroom type management</li>
 * <li>Search by name</li>
 * <li>Ordered retrieval for UI display</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ClassroomTypeRepository extends JpaRepository<ClassroomType, Long> {
    
    /**
     * Find classroom type by name (case insensitive).
     * 
     * @param name the classroom type name
     * @return optional classroom type if found
     */
    Optional<ClassroomType> findByNameIgnoreCase(String name);
    
    /**
     * Check if a classroom type with the given name exists.
     * 
     * @param name the classroom type name
     * @return true if exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Find all classroom types ordered by name.
     * 
     * @return list of all classroom types sorted by name
     */
    @Query("SELECT ct FROM ClassroomType ct ORDER BY ct.name ASC")
    List<ClassroomType> findAllOrderedByName();
}
