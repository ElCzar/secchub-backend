package co.edu.puj.secchub_backend.planning.repository;

import co.edu.puj.secchub_backend.planning.model.ClassroomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ClassroomType entity.
 * Provides data access operations for classroom type management in the planning module.
 */
@Repository
public interface ClassroomTypeRepository extends JpaRepository<ClassroomType, Long> {
    
    /**
     * Find classroom type by name.
     * @param name the classroom type name
     * @return optional classroom type with the specified name
     */
    Optional<ClassroomType> findByName(String name);
    
    /**
     * Check if classroom type exists by name.
     * @param name the classroom type name
     * @return true if classroom type exists, false otherwise
     */
    boolean existsByName(String name);
}