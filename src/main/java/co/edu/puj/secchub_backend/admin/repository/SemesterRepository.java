package co.edu.puj.secchub_backend.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.puj.secchub_backend.admin.model.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByIsCurrentTrue();
    Optional<Semester> findByYearAndPeriod(Integer year, Integer period);
}
