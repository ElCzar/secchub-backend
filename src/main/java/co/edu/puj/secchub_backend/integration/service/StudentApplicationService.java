package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.*;
import co.edu.puj.secchub_backend.integration.model.*;
import co.edu.puj.secchub_backend.integration.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentApplicationService {

    private final StudentRepository studentRepo;
    private final StudentScheduleRepository scheduleRepo;

    @Transactional
    public Student createRequest(StudentApplicationDTO dto) {
        Student student = Student.builder()
                .userId(dto.getUserId())
                .courseId(dto.getCourseId())
                .sectionId(dto.getSectionId())
                .courseAverage(dto.getCourseAverage())
                .courseTeacher(dto.getCourseTeacher())
                .applicationDate(LocalDate.now())
                .statusId(dto.getStatusId())
                .build();

        Student saved = studentRepo.save(student);

        if (dto.getSchedules() != null) {
            for (ScheduleDTO s : dto.getSchedules()) {
                StudentSchedule sched = StudentSchedule.builder()
                        .studentId(saved.getId())
                        .day(s.getDay())
                        .startTime(Time.valueOf(s.getStartTime()))
                        .endTime(Time.valueOf(s.getEndTime()))
                        .build();
                scheduleRepo.save(sched);
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Student> listAll() {
        return studentRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Student> listByStatus(Long statusId) {
        return studentRepo.findByStatusId(statusId);
    }

    @Transactional(readOnly = true)
    public List<Student> listForSection(Long sectionId) {
        return studentRepo.findRequestsForSection(sectionId);
    }

    @Transactional
    public void approveRequest(Long id, Long statusApprovedId) {
        Student student = findById(id);
        student.setStatusId(statusApprovedId);
        studentRepo.save(student);
    }

    @Transactional
    public void rejectRequest(Long id, Long statusRejectedId) {
        Student student = findById(id);
        student.setStatusId(statusRejectedId);
        studentRepo.save(student);
    }
}
