package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.StudentApplication;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;

public interface StudentApplicationRepository extends R2dbcRepository<StudentApplication, Long> {
    @Query("SELECT * FROM student_application WHERE semester_id = :semesterId")
    Flux<StudentApplication> findBySemesterId(@Param("semesterId") Long semesterId);
    
    @Query("SELECT * FROM student_application WHERE status_id = :statusId")
    Flux<StudentApplication> findByStatusId(@Param("statusId") Long statusId);

    @Query("""
        SELECT * FROM student_application
        WHERE (section_id = :sectionId)
            OR (course_id IN (SELECT id FROM course WHERE section_id = :sectionId))
    """)
    Flux<StudentApplication> findRequestsForSection(@Param("sectionId") Long sectionId);

    @Query("SELECT * FROM student_application WHERE user_id = :userId AND semester_id = :semesterId")
    Flux<StudentApplication> findByUserIdAndSemesterId(@Param("userId") Long userId, @Param("semesterId") Long semesterId);
}