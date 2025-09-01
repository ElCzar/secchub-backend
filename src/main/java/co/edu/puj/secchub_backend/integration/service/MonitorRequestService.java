package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.*;
import co.edu.puj.secchub_backend.integration.model.*;
import co.edu.puj.secchub_backend.integration.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that orchestrates the flow of monitor requests:
 * - Create requests with availability
 * - List requests
 * - Approve or reject requests
 */
@Service
@RequiredArgsConstructor
public class MonitorRequestService {

    private final MonitorRequestRepository requestRepo;
    private final MonitorAvailabilityRepository availabilityRepo;

    /**
     * Creates a new monitor request with availability slots.
     */
    @Transactional
    public MonitorRequest createRequest(MonitorRequestDTO dto) {
        MonitorRequest request = MonitorRequest.builder()
                .studentId(dto.getStudentId())
                .semesterId(dto.getSemesterId())
                .type(dto.getType())
                .courseId(dto.getCourseId())
                .sectionId(dto.getSectionId())
                .grade(dto.getGrade())
                .professorName(dto.getProfessorName())
                .statusId(dto.getStatusId())
                .requestDate(LocalDate.now())
                .build();

        MonitorRequest saved = requestRepo.save(request);

        if (dto.getAvailabilities() != null) {
            for (MonitorAvailabilityDTO a : dto.getAvailabilities()) {
                MonitorAvailability slot = MonitorAvailability.builder()
                        .monitorRequestId(saved.getId())
                        .day(a.getDay())
                        .startTime(java.sql.Time.valueOf(a.getStartTime()))
                        .endTime(java.sql.Time.valueOf(a.getEndTime()))
                        .totalHours(a.getTotalHours())
                        .build();
                availabilityRepo.save(slot);
            }
        }
        return saved;
    }

    /**
     * Returns all monitor requests.
     */
    @Transactional(readOnly = true)
    public List<MonitorRequest> listAll() {
        return requestRepo.findAll();
    }

    /**
     * Approves a request by setting its status.
     */
    @Transactional
    public void approveRequest(Long id, Long statusApprovedId) {
        MonitorRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MonitorRequest not found: " + id));
        req.setStatusId(statusApprovedId);
        requestRepo.save(req);
    }

    /**
     * Rejects a request by setting its status.
     */
    @Transactional
    public void rejectRequest(Long id, Long statusRejectedId) {
        MonitorRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MonitorRequest not found: " + id));
        req.setStatusId(statusRejectedId);
        requestRepo.save(req);
    }

    /**
     * Finds a monitor request by id.
     */
    @Transactional(readOnly = true)
    public MonitorRequest findById(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MonitorRequest not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<MonitorRequest> listByStatus(Long statusId) {
        return requestRepo.findByStatusId(statusId);
    }


    @Transactional(readOnly = true)
    public List<MonitorRequest> listForSection(Long sectionId) {
        return requestRepo.findRequestsForSection(sectionId);
    }




}
