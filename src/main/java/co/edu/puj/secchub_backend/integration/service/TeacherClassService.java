package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * @return list of TeacherClass
     */
    @Transactional(readOnly = true)
    public List<TeacherClass> listAllByTeacher(Long teacherId) {
        return repository.findByTeacherId(teacherId);
    }

    /**
     * Lists only classes filtered by status.
     * @param teacherId teacher id
     * @param statusId status (1=pending, 2=accepted, 3=rejected)
     * @return list of TeacherClass
     */
    @Transactional(readOnly = true)
    public List<TeacherClass> listByStatus(Long teacherId, Long statusId) {
        return repository.findByTeacherIdAndStatusId(teacherId, statusId);
    }

        /**
     * Accepts a class (set decision true and status=2).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClass
     */
    @Transactional
    public TeacherClass acceptClass(Long teacherClassId, String observation) {
        TeacherClass tc = repository.findById(teacherClassId)
                .orElseThrow(() -> new IllegalArgumentException("TeacherClass not found"));
        tc.setDecision(true);
        tc.setStatusId(2L);
        tc.setObservation(observation);
        return repository.save(tc);
    }

    /**
     * Rejects a class (set decision false and status=3).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClass
     */
    @Transactional
    public TeacherClass rejectClass(Long teacherClassId, String observation) {
        TeacherClass tc = repository.findById(teacherClassId)
                .orElseThrow(() -> new IllegalArgumentException("TeacherClass not found"));
        tc.setDecision(false);
        tc.setStatusId(3L);
        tc.setObservation(observation);
        return repository.save(tc);
    }

}
