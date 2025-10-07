package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentApplicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentApplicationScheduleRepository extends JpaRepository<StudentApplicationSchedule, Long> {
    List<StudentApplicationSchedule> findByStudentApplicationId(Long studentApplicationId);
}