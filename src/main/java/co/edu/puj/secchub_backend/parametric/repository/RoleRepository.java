package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}