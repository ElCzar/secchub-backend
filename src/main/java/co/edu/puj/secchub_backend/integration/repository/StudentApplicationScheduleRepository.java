package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentApplicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentApplicationScheduleRepository extends JpaRepository<StudentApplicationSchedule, Long> {
    
    @Query("SELECT s FROM StudentApplicationSchedule s WHERE s.studentApplication.id = :studentApplicationId")
    List<StudentApplicationSchedule> findByStudentApplicationId(@Param("studentApplicationId") Long studentApplicationId);
}