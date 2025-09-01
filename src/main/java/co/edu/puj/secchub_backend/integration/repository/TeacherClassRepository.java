package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TeacherClass entity.
 * Provides access to class assignments of teachers for HU17.
 */
@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {

    List<TeacherClass> findByTeacherId(Long teacherId);

    List<TeacherClass> findByTeacherIdAndStatusId(Long teacherId, Long statusId);
}
