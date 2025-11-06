package co.edu.puj.secchub_backend.admin.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.admin.model.Course;
import reactor.core.publisher.Mono;

/**
 * Repository for CRUD operations on Course entity.
 */
@Repository
public interface CourseRepository extends R2dbcRepository<Course, Long> {
    Mono<Boolean> existsByName(String name);
}
