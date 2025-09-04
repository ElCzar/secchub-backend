package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.exception.TeacherClassNotFoundException;
import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Service handling business logic for HU17 (Professor availability).
 * A teacher can see pending classes and accept/reject them.
 */
@Service
@RequiredArgsConstructor
public class TeacherClassService {

    private final TeacherClassRepository repository;

    /**
     * Lists all classes (accepted, pending, rejected) assigned to a teacher.
     * @param teacherId teacher id
     * @return Stream of TeacherClass
     */
    public Flux<TeacherClass> listAllTeacherClassByTeacher(Long teacherId) {
        return Mono.fromCallable(() -> repository.findByTeacherId(teacherId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists only classes filtered by status.
     * @param teacherId teacher id
     * @param statusId status (1=pending, 2=accepted, 3=rejected)
     * @return flux of TeacherClass
     */
    public Flux<TeacherClass> listTeacherClassByStatus(Long teacherId, Long statusId) {
        return Mono.fromCallable(() -> repository.findByTeacherIdAndStatusId(teacherId, statusId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Accepts a class (set decision true and status=2).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClass
     */
    @Transactional
    public Mono<TeacherClass> acceptTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for acceptance: " + teacherClassId));
            tc.setDecision(true);
            tc.setStatusId(2L);
            tc.setObservation(observation);
            return repository.save(tc);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Rejects a class (set decision false and status=3).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClass
     */
    @Transactional
    public Mono<TeacherClass> rejectTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for rejection: " + teacherClassId));
            tc.setDecision(false);
            tc.setStatusId(3L);
            tc.setObservation(observation);
            return repository.save(tc);
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
