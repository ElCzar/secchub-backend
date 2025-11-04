package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.ClassroomType;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ClassroomType entity.
 * Provides data access operations for classroom type management in the planning module.
 */
@Repository
public interface ClassroomTypeRepository extends R2dbcRepository<ClassroomType, Long> {
    
    /**
     * Find classroom type by name.
     * @param name the classroom type name
     * @return mono classroom type with the specified name
     */
    Mono<ClassroomType> findByName(String name);
    
    /**
     * Check if classroom type exists by name.
     * @param name the classroom type name
     * @return true if classroom type exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);
}