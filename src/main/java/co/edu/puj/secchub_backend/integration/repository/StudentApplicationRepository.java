package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {
    List<StudentApplication> findByStatusId(Long statusId);

    @Query("""
        SELECT s FROM StudentApplication s
        WHERE (s.sectionId = :sectionId)
           OR (s.courseId IN (SELECT c.id FROM Course c WHERE c.sectionId = :sectionId))
    """)
    List<StudentApplication> findRequestsForSection(@Param("sectionId") Long sectionId);
}