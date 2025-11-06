package co.edu.puj.secchub_backend.parametric.repository;

import co.edu.puj.secchub_backend.parametric.model.Role;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {
    
    Mono<Role> findByName(String name);
    Mono<Boolean> existsByName(String name);
}