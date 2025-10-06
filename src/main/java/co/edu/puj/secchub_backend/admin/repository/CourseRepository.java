package co.edu.puj.secchub_backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.puj.secchub_backend.admin.model.Course;

/**
 * Repository for CRUD operations on Course entity.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
