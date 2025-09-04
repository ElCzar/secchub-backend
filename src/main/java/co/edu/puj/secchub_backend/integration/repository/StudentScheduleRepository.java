package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentScheduleRepository extends JpaRepository<StudentSchedule, Long> {
    List<StudentSchedule> findByStudentId(Long studentId);
}