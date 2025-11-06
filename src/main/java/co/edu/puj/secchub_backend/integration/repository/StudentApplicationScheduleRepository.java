package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentApplicationSchedule;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;

public interface StudentApplicationScheduleRepository extends R2dbcRepository<StudentApplicationSchedule, Long> {
    
    @Query("SELECT * FROM student_application_schedule WHERE student_application_id = :studentApplicationId")
    Flux<StudentApplicationSchedule> findByStudentApplicationId(@Param("studentApplicationId") Long studentApplicationId);
}