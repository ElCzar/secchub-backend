package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestScheduleRepository extends JpaRepository<RequestSchedule, Long> {
    List<RequestSchedule> findByAcademicRequestId(Long academicRequestId);
}
