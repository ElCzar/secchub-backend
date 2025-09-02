package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStatusId(Long statusId);

    @Query("""
        SELECT s FROM Student s
        WHERE (s.sectionId = :sectionId)
           OR (s.courseId IN (SELECT c.id FROM Course c WHERE c.section.id = :sectionId))
    """)
    List<Student> findRequestsForSection(@Param("sectionId") Long sectionId);
}