package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface RequestScheduleRepository extends R2dbcRepository<RequestSchedule, Long> {
    Flux<RequestSchedule> findByAcademicRequestId(Long academicRequestId);
}
