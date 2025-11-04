package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.dto.StudentApplicationResponseDTO;
import co.edu.puj.secchub_backend.integration.model.StudentApplication;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentApplicationRepository extends R2dbcRepository<StudentApplication, Long> {
    Flux<StudentApplication> findBySemesterId(Long semesterId);
    
    Flux<StudentApplication> findByStatusId(Long statusId);

    @Query("""
        SELECT s FROM StudentApplication s
        WHERE (s.sectionId = :sectionId)
            OR (s.courseId IN (SELECT c.id FROM Course c WHERE c.sectionId = :sectionId))
    """)
    Flux<StudentApplication> findRequestsForSection(@Param("sectionId") Long sectionId);

    Mono<StudentApplicationResponseDTO> findByUserIdAndSemesterId(Long userId, Long semesterId);
}