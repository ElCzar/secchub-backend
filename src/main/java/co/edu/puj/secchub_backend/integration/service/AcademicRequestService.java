package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleDTO;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import co.edu.puj.secchub_backend.integration.repository.AcademicRequestRepository;
import co.edu.puj.secchub_backend.integration.repository.RequestScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcademicRequestService {

    private final AcademicRequestRepository requestRepo;
    private final RequestScheduleRepository scheduleRepo;

    private final ValidationService validation;
    private final PdfGenerator pdfGenerator;
    private final EmailService emailService;

    /**
     * Crea un lote de solicitudes académicas con sus horarios asociados.
     */
    @Transactional
    public List<AcademicRequest> createBatch(AcademicRequestBatchDTO payload) {
        List<AcademicRequest> results = new ArrayList<>();

        for (AcademicRequestDTO item : payload.getRequests()) {
            AcademicRequest ar = AcademicRequest.builder()
                    .userId(payload.getUserId())
                    .courseId(item.getCourseId())
                    .semesterId(payload.getSemesterId())
                    .capacity(item.getCapacity())
                    .startDate(item.getStartDate())
                    .endDate(item.getEndDate())
                    .observation(item.getObservation())
                    .build();

    AcademicRequest saved = requestRepo.save(ar);

    if (item.getSchedules() != null) {
        for (RequestScheduleDTO s : item.getSchedules()) {
            RequestSchedule sch = RequestSchedule.builder()
                    .academicRequestId(saved.getId())
                    .day(s.getDay())
                    .startTime(toSqlTime(s.getStartTime()))
                    .endTime(toSqlTime(s.getEndTime()))
                    .classroomTypeId(s.getClassroomTypeId())
                    .modalityId(s.getModalityId())
                    .disability(s.getDisability())
                    .build();
            scheduleRepo.save(sch);
        }
        // 🚀 Cargar los schedules recién guardados
        saved.setSchedules(scheduleRepo.findByAcademicRequestId(saved.getId()));
    }

    results.add(saved);

            }


        return results;
    }

    /**
     * Obtiene todas las solicitudes académicas.
     */
    @Transactional(readOnly = true)
    public List<AcademicRequest> findAll() {
        return requestRepo.findAll();
    }

    /**
     * Obtiene una solicitud académica por ID.
     */
    @Transactional(readOnly = true)
    public AcademicRequest findById(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + id));
    }

    /**
     * Elimina una solicitud académica por ID.
     */
    @Transactional
    public void deleteRequest(Long requestId) {
        if (!requestRepo.existsById(requestId)) {
            throw new IllegalArgumentException("AcademicRequest not found: " + requestId);
        }
        requestRepo.deleteById(requestId);
    }

    /**
     * Actualiza una solicitud académica.
     */
    @Transactional
    public AcademicRequest updateRequest(Long id, AcademicRequestDTO dto) {
        AcademicRequest request = requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + id));

        request.setCourseId(dto.getCourseId());
        request.setCapacity(dto.getCapacity());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setObservation(dto.getObservation());

        return requestRepo.save(request);
    }

    /**
     * Agrega un horario a una solicitud académica.
     */
    @Transactional
    public RequestScheduleDTO addSchedule(Long requestId, RequestScheduleDTO dto) {
        AcademicRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + requestId));

        RequestSchedule schedule = RequestSchedule.builder()
                .academicRequestId(request.getId())
                .day(dto.getDay())
                .startTime(toSqlTime(dto.getStartTime()))
                .endTime(toSqlTime(dto.getEndTime()))
                .classroomTypeId(dto.getClassroomTypeId())
                .modalityId(dto.getModalityId())
                .disability(dto.getDisability())
                .build();

        scheduleRepo.save(schedule);

        dto.setId(schedule.getId());
        return dto;
    }

    /**
     * Obtiene los horarios asociados a una solicitud.
     */
    @Transactional(readOnly = true)
    public List<RequestScheduleDTO> findSchedulesByRequest(Long requestId) {
        requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + requestId));

        List<RequestSchedule> schedules = scheduleRepo.findByAcademicRequestId(requestId);
        List<RequestScheduleDTO> dtos = new ArrayList<>();

        for (RequestSchedule s : schedules) {
            dtos.add(RequestScheduleDTO.builder()
                    .id(s.getId())
                    .day(s.getDay())
                    .startTime(toStringTime(s.getStartTime()))
                    .endTime(toStringTime(s.getEndTime()))
                    .classroomTypeId(s.getClassroomTypeId())
                    .modalityId(s.getModalityId())
                    .disability(s.getDisability())
                    .build());
        }
        return dtos;
    }

    /**
     * Actualiza un horario específico.
     */
    @Transactional
    public RequestScheduleDTO updateSchedule(Long scheduleId, RequestScheduleDTO dto) {
        RequestSchedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("RequestSchedule not found: " + scheduleId));

        if (dto.getDay() != null) schedule.setDay(dto.getDay());
        if (dto.getStartTime() != null) schedule.setStartTime(toSqlTime(dto.getStartTime()));
        if (dto.getEndTime() != null) schedule.setEndTime(toSqlTime(dto.getEndTime()));
        if (dto.getClassroomTypeId() != null) schedule.setClassroomTypeId(dto.getClassroomTypeId());
        if (dto.getModalityId() != null) schedule.setModalityId(dto.getModalityId());
        if (dto.getDisability() != null) schedule.setDisability(dto.getDisability());

        scheduleRepo.save(schedule);

        return RequestScheduleDTO.builder()
                .id(schedule.getId())
                .day(schedule.getDay())
                .startTime(toStringTime(schedule.getStartTime()))
                .endTime(toStringTime(schedule.getEndTime()))
                .classroomTypeId(schedule.getClassroomTypeId())
                .modalityId(schedule.getModalityId())
                .disability(schedule.getDisability())
                .build();
    }

    /**
     * Elimina un horario por ID.
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepo.existsById(scheduleId)) {
            throw new IllegalArgumentException("RequestSchedule not found: " + scheduleId);
        }
        scheduleRepo.deleteById(scheduleId);
    }

    /**
     * Actualiza parcialmente un horario.
     */
    @Transactional
    public RequestScheduleDTO patchSchedule(Long scheduleId, Map<String, Object> updates) {
        RequestSchedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("RequestSchedule not found: " + scheduleId));

        if (updates.containsKey("day")) schedule.setDay((String) updates.get("day"));
        if (updates.containsKey("startTime")) schedule.setStartTime(toSqlTime((String) updates.get("startTime")));
        if (updates.containsKey("endTime")) schedule.setEndTime(toSqlTime((String) updates.get("endTime")));
        if (updates.containsKey("classroomTypeId")) schedule.setClassroomTypeId(Long.valueOf(updates.get("classroomTypeId").toString()));
        if (updates.containsKey("modalityId")) schedule.setModalityId(Long.valueOf(updates.get("modalityId").toString()));
        if (updates.containsKey("disability")) schedule.setDisability(Boolean.valueOf(updates.get("disability").toString()));

        scheduleRepo.save(schedule);

        return RequestScheduleDTO.builder()
                .id(schedule.getId())
                .day(schedule.getDay())
                .startTime(toStringTime(schedule.getStartTime()))
                .endTime(toStringTime(schedule.getEndTime()))
                .classroomTypeId(schedule.getClassroomTypeId())
                .modalityId(schedule.getModalityId())
                .disability(schedule.getDisability())
                .build();
    }

    // ======================
    // Utilidades privadas
    // ======================

    private java.sql.Time toSqlTime(String hhmmss) {
        if (hhmmss == null) return null;
        LocalTime t = LocalTime.parse(hhmmss);
        return java.sql.Time.valueOf(t);
    }

    private String toStringTime(java.sql.Time time) {
        return time != null ? time.toLocalTime().toString() : null;
    }

    public int calculateWeeks(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        long days = ChronoUnit.DAYS.between(start, end.plusDays(1));
        return (int) Math.ceil(days / 7.0);
    }
}
