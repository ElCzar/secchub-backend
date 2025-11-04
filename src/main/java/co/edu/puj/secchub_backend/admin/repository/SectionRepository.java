package co.edu.puj.secchub_backend.admin.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import co.edu.puj.secchub_backend.admin.model.Section;
import reactor.core.publisher.Mono;

public interface SectionRepository extends R2dbcRepository<Section, Long> {
    Mono<Section> findByUserId(Long userId);
}