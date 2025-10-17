package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AcademicRequestRepository extends JpaRepository<AcademicRequest, Long> {
    List<AcademicRequest> findBySemesterId(Long semesterId);
    List<AcademicRequest> findByCourseId(Long courseId);
    List<AcademicRequest> findBySemesterIdAndUserId(Long semesterId, Long userId);
}
