package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for TeacherClass entity.
 * Provides access to class assignments of teachers for HU17.
 */
@Repository
public interface TeacherClassRepository extends R2dbcRepository<TeacherClass, Long> {

    Flux<TeacherClass> findBySemesterId(Long semesterId);

    Flux<TeacherClass> findByTeacherId(Long teacherId);

    Flux<TeacherClass> findByStatusId(Long statusId);

    Flux<TeacherClass> findByClassId(Long classId);

    Flux<TeacherClass> findByTeacherIdAndStatusId(Long teacherId, Long statusId);

    Flux<TeacherClass> findBySemesterIdAndTeacherId(Long semesterId, Long teacherId);

    Mono<TeacherClass> findByTeacherIdAndClassId(Long teacherId, Long classId);
}
