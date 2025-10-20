package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TeacherClass entity.
 * Provides access to class assignments of teachers for HU17.
 */
@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {

    List<TeacherClass> findBySemesterId(Long semesterId);

    List<TeacherClass> findByTeacherId(Long teacherId);

    List<TeacherClass> findByStatusId(Long statusId);

    List<TeacherClass> findByClassId(Long classId);

    List<TeacherClass> findByTeacherIdAndStatusId(Long teacherId, Long statusId);

    List<TeacherClass> findBySemesterIdAndTeacherId(Long semesterId, Long teacherId);
}
