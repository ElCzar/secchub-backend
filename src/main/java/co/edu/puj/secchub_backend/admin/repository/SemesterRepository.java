package co.edu.puj.secchub_backend.admin.repository;

import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import co.edu.puj.secchub_backend.admin.model.Semester;

public interface SemesterRepository extends R2dbcRepository<Semester, Long> {
    Mono<Semester> findByIsCurrentTrue();
    Mono<Semester> findByYearAndPeriod(Integer year, Integer period);
}
