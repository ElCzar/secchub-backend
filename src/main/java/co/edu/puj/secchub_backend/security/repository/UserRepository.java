package co.edu.puj.secchub_backend.security.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.puj.secchub_backend.security.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Flux<User> findByRoleId(Long roleId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastAccess = CURRENT_TIMESTAMP WHERE u.email = :email")
    void updateLastAccess(@Param("email") String email);
}