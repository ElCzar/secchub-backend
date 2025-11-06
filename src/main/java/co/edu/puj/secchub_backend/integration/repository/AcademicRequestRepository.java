package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicRequestRepository extends R2dbcRepository<AcademicRequest, Long> {
    Flux<AcademicRequest> findBySemesterId(Long semesterId);
    Flux<AcademicRequest> findByCourseId(Long courseId);
    Flux<AcademicRequest> findBySemesterIdAndUserId(Long semesterId, Long userId);
}
