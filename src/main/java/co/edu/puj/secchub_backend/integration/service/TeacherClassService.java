package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassNotFoundException;
import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Service handling business logic for HU17 (Professor availability).
 * A teacher can see pending classes and accept/reject them.
 */
@Service
@RequiredArgsConstructor
public class TeacherClassService {
    // TODO: Add notifications when an admin/user wants to inform a teacher of a new class assignment

    private final TeacherClassRepository repository;
    private final ModelMapper modelMapper;
    private final AdminModuleSemesterContract semesterService;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_IN_PROGRESS_ID = 6L;
    private static final Long STATUS_ACCEPTED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    /**
     * Creates a new teacher-class assignment.
     * @param TeacherClassRequestDTO with assignment data
     * @return TeacherClassResponseDTO with created assignment
     */
    public Mono<TeacherClassResponseDTO> createTeacherClass(co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO request) {
        return Mono.fromCallable(() -> {
            TeacherClass teacherClass = modelMapper.map(request, TeacherClass.class);
            teacherClass.setStatusId(STATUS_PENDING_ID);
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            teacherClass.setSemesterId(currentSemesterId);
            TeacherClass saved = repository.save(teacherClass);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists all teacher classes for the current semester.
     * @return List of TeacherClassResponseDTO for the current semester
     */
    public List<TeacherClassResponseDTO> listCurrentSemesterTeacherClasses() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return repository.findBySemesterId(currentSemesterId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists all classes (accepted, pending, rejected) assigned to a teacher.
     * @param teacherId teacher id
     * @return List of TeacherClassResponseDTO
     */
    public List<TeacherClassResponseDTO> listAllTeacherClassByTeacher(Long teacherId) {
        return repository.findByTeacherId(teacherId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists only classes filtered by status.
     * @param teacherId teacher id
     * @param statusId status (1=pending, 2=accepted, 3=rejected)
     * @return List of TeacherClassResponseDTO
     */
    public List<TeacherClassResponseDTO> listTeacherClassByStatus(Long teacherId, Long statusId) {
        return repository.findByTeacherIdAndStatusId(teacherId, statusId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Lists all classes for a given class ID.
     * @param classId
     * @return List of TeacherClassResponseDTO for the given class ID
     */
    public List<TeacherClassResponseDTO> listTeacherClassByClassId(Long classId) {
        return repository.findByClassId(classId).stream()
                .map(teacherClass -> modelMapper.map(teacherClass, TeacherClassResponseDTO.class))
                .toList();
    }

    /**
     * Accepts a class (set decision true and status=2).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    @Transactional
    public Mono<TeacherClassResponseDTO> acceptTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for acceptance: " + teacherClassId));
            tc.setDecision(true);
            tc.setStatusId(STATUS_ACCEPTED_ID);
            tc.setObservation(observation);
            TeacherClass saved = repository.save(tc);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Rejects a class (set decision false and status=3).
     * @param teacherClassId relation id
     * @param observation optional comment from the teacher
     * @return updated TeacherClassResponseDTO
     */
    @Transactional
    public Mono<TeacherClassResponseDTO> rejectTeacherClass(Long teacherClassId, String observation) {
        return Mono.fromCallable(() -> {
            TeacherClass tc = repository.findById(teacherClassId)
                    .orElseThrow(() -> new TeacherClassNotFoundException("TeacherClass not found for rejection: " + teacherClassId));
            tc.setDecision(false);
            tc.setStatusId(STATUS_REJECTED_ID);
            tc.setObservation(observation);
            TeacherClass saved = repository.save(tc);
            return modelMapper.map(saved, TeacherClassResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
