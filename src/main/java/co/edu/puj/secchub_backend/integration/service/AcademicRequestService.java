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

/**
 * Servicio para la gestión de solicitudes académicas y sus horarios asociados.
 * Orquesta la validación, persistencia, generación de PDF y envío de correos electrónicos.
 */
@Service
@RequiredArgsConstructor
public class AcademicRequestService {

    /**
     * Repositorio para solicitudes académicas.
     */
    private final AcademicRequestRepository requestRepo;
    /**
     * Repositorio para horarios de solicitudes académicas.
     */
    private final RequestScheduleRepository scheduleRepo;
    /**
     * Servicio de validación de datos.
     */
    private final ValidationService validation;
    /**
     * Servicio para generación de PDF.
     */
    private final PdfGenerator pdfGenerator;
    /**
     * Servicio para envío de correos electrónicos.
     */
    private final EmailService emailService;

    /**
     * Persiste un lote de solicitudes académicas y sus horarios asociados.
     * @param payload DTO con la información del lote
     * @return Lista de entidades guardadas
     */
    @Transactional
    public List<AcademicRequest> createBatch(AcademicRequestBatchDTO payload) {
        List<AcademicRequest> results = new ArrayList<>();

        for (AcademicRequestDTO item : payload.getRequests()) {
            AcademicRequest ar = AcademicRequest.builder()
                    .userId(payload.getUserId())
                    .courseId(item.getCourseId())
                    .semesterId(payload.getSemesterId())
                    .observation(item.getObservation())
                    .section(item.getSection())
                    .classroomTypeId(item.getClassroomTypeId())
                    .requestedQuota(item.getRequestedQuota())
                    .startDate(item.getStartDate())
                    .endDate(item.getEndDate())
                    // si weeks no viene calculado, lo calculamos
                    .weeks(item.getWeeks() != null
                           ? item.getWeeks()
                           : calculateWeeks(item.getStartDate(), item.getEndDate()))
                    .build();

            AcademicRequest saved = requestRepo.save(ar);

            // Guardar horarios si existen
            if (item.getSchedules() != null) {
                for (RequestScheduleDTO s : item.getSchedules()) {
                    RequestSchedule sch = RequestSchedule.builder()
                            .academicRequestId(saved.getId())
                            .day(s.getDay())
                            .startTime(toIntHHmm(s.getStartTime()))
                            .endTime(toIntHHmm(s.getEndTime()))
                            .classroomTypeId(s.getClassroomTypeId())
                            .modalityId(s.getModalityId())
                            .disability(s.getDisability())
                            .build();
                    scheduleRepo.save(sch);
                }
            }

            results.add(saved);
        }

        // aquí podrías disparar pdfGenerator y emailService con payload

        return results;
    }

    /**
     * Lista solicitudes académicas filtradas por sección, semestre o curso.
     * @param section Sección
     * @param semesterId ID de semestre
     * @param courseId ID de curso
     * @return Lista de solicitudes
     */
    @Transactional(readOnly = true)
    public List<AcademicRequest> list(String section, Long semesterId, Long courseId) {
        if (section != null) return requestRepo.findBySection(section);
        if (semesterId != null) return requestRepo.findBySemesterId(semesterId);
        if (courseId != null) return requestRepo.findByCourseId(courseId);
        return requestRepo.findAll();
    }

    /**
     * Calcula el número de semanas entre dos fechas.
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Número de semanas
     */
    private int calculateWeeks(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        long days = ChronoUnit.DAYS.between(start, end.plusDays(1));
        return (int) Math.ceil(days / 7.0);
    }

    /**
     * Convierte una cadena de hora en formato "HH:mm:ss" a entero HHmm.
     * @param hhmmss Hora en formato cadena
     * @return Hora en formato entero
     */
    private int toIntHHmm(String hhmmss) {
    LocalTime t = LocalTime.parse(hhmmss);
    return t.getHour() * 100 + t.getMinute();
    }


    /**
     * Agrega un horario a una solicitud académica.
     * @param requestId ID de la solicitud
     * @param dto DTO del horario
     * @return DTO del horario creado
     */
    @Transactional
    public RequestScheduleDTO addSchedule(Long requestId, RequestScheduleDTO dto) {
        AcademicRequest request = requestRepo.findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + requestId));

        // convierte "HH:mm:ss" a int HHmm → ej. "14:30:00" → 1430
        int startInt = toIntHHmm(dto.getStartTime());
        int endInt   = toIntHHmm(dto.getEndTime());

        RequestSchedule schedule = RequestSchedule.builder()
                .academicRequestId(request.getId())
                .day(dto.getDay())
                .startTime(startInt)   // usa tu builder custom
                .endTime(endInt)       // usa tu builder custom
                .classroomTypeId(dto.getClassroomTypeId())
                .modalityId(dto.getModalityId())
                .disability(dto.getDisability())
                .build();

        scheduleRepo.save(schedule);

        // devuelve el DTO con id generado
        dto.setId(schedule.getId());
        return dto;
    }

    /**
     * Elimina una solicitud académica por su ID.
     * @param requestId ID de la solicitud
     */
    @Transactional
    public void deleteRequest(Long requestId) {
        if (!requestRepo.existsById(requestId)) {
            throw new IllegalArgumentException("AcademicRequest not found: " + requestId);
        }
        requestRepo.deleteById(requestId);
    }

    /**
     * Elimina un horario por su ID.
     * @param scheduleId ID del horario
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepo.existsById(scheduleId)) {
            throw new IllegalArgumentException("RequestSchedule not found: " + scheduleId);
        }
        scheduleRepo.deleteById(scheduleId);
    }

    /**
     * Obtiene todas las solicitudes académicas.
     * @return Lista de solicitudes
     */
    public List<AcademicRequest> findAll() {
        return requestRepo.findAll();
    }

    /**
     * Obtiene una solicitud académica por su ID.
     * @param id ID de la solicitud
     * @return Solicitud encontrada
     */
    public AcademicRequest findById(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + id));
    }

    /**
     * Actualiza una solicitud académica por su ID.
     * @param id ID de la solicitud
     * @param dto DTO con los datos actualizados
     * @return Solicitud actualizada
     */
    @Transactional
    public AcademicRequest updateRequest(Long id, AcademicRequestDTO dto) {
        AcademicRequest request = requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + id));

        request.setObservation(dto.getObservation());
        request.setSection(dto.getSection());
        request.setClassroomTypeId(dto.getClassroomTypeId());
        request.setRequestedQuota(dto.getRequestedQuota());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setWeeks(dto.getWeeks() != null
                ? dto.getWeeks()
                : calculateWeeks(dto.getStartDate(), dto.getEndDate()));

        return requestRepo.save(request);
    }

    /**
     * Obtiene los horarios asociados a una solicitud académica.
     * @param requestId ID de la solicitud
     * @return Lista de horarios en formato DTO
     */
    @Transactional(readOnly = true)
    public List<RequestScheduleDTO> findSchedulesByRequest(Long requestId) {
    AcademicRequest request = requestRepo.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("AcademicRequest not found: " + requestId));

    List<RequestSchedule> schedules = scheduleRepo.findByAcademicRequestId(requestId);

    List<RequestScheduleDTO> dtos = new ArrayList<>();
    for (RequestSchedule s : schedules) {
        String start = s.getStartTime() != null ? s.getStartTime().toLocalTime().toString() : null;
        String end   = s.getEndTime()   != null ? s.getEndTime().toLocalTime().toString()   : null;

        dtos.add(RequestScheduleDTO.builder()
                .id(s.getId())
                .day(s.getDay())
                .startTime(start)    // "HH:mm:ss"
                .endTime(end)        // "HH:mm:ss"
                .classroomTypeId(s.getClassroomTypeId())
                .modalityId(s.getModalityId())
                .disability(s.getDisability())
                .build());
    }
    return dtos;
}

    /**
     * Actualiza un horario específico de una solicitud académica.
     * @param scheduleId ID del horario
     * @param dto DTO con los datos actualizados
     * @return DTO del horario actualizado
     */
    @Transactional
    public RequestScheduleDTO updateSchedule(Long scheduleId, RequestScheduleDTO dto) {
    RequestSchedule schedule = scheduleRepo.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("RequestSchedule not found: " + scheduleId));

    
    if (dto.getDay() != null) {
        schedule.setDay(dto.getDay());
    }
    if (dto.getStartTime() != null) {
        int startInt = toIntHHmm(dto.getStartTime()); // "10:00:00" -> 1000
        schedule.setStartTime(java.sql.Time.valueOf(
                String.format("%02d:%02d:00", startInt / 100, startInt % 100)));
    }
    if (dto.getEndTime() != null) {
        int endInt = toIntHHmm(dto.getEndTime()); // "12:00:00" -> 1200
        schedule.setEndTime(java.sql.Time.valueOf(
                String.format("%02d:%02d:00", endInt / 100, endInt % 100)));
    }
    if (dto.getClassroomTypeId() != null) {
        schedule.setClassroomTypeId(dto.getClassroomTypeId());
    }
    if (dto.getModalityId() != null) {
        schedule.setModalityId(dto.getModalityId());
    }
    if (dto.getDisability() != null) {
        schedule.setDisability(dto.getDisability());
    }

    
    scheduleRepo.save(schedule);

    
    return RequestScheduleDTO.builder()
            .id(schedule.getId())
            .day(schedule.getDay())
            .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : null)
            .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toLocalTime().toString() : null)
            .classroomTypeId(schedule.getClassroomTypeId())
            .modalityId(schedule.getModalityId())
            .disability(schedule.getDisability())
            .build();
}

    /**
     * Actualiza parcialmente un horario específico de una solicitud académica.
     * @param scheduleId ID del horario
     * @param updates Mapa con los campos a actualizar
     * @return DTO del horario actualizado parcialmente
     */
    @Transactional
    public RequestScheduleDTO patchSchedule(Long scheduleId, Map<String, Object> updates) {
    RequestSchedule schedule = scheduleRepo.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("RequestSchedule not found: " + scheduleId));

    if (updates.containsKey("day")) {
        schedule.setDay((String) updates.get("day"));
    }
    if (updates.containsKey("startTime")) {
        int startInt = toIntHHmm((String) updates.get("startTime"));
        schedule.setStartTime(java.sql.Time.valueOf(
                String.format("%02d:%02d:00", startInt / 100, startInt % 100)));
    }
    if (updates.containsKey("endTime")) {
        int endInt = toIntHHmm((String) updates.get("endTime"));
        schedule.setEndTime(java.sql.Time.valueOf(
                String.format("%02d:%02d:00", endInt / 100, endInt % 100)));
    }
    if (updates.containsKey("classroomTypeId")) {
        schedule.setClassroomTypeId(Long.valueOf(updates.get("classroomTypeId").toString()));
    }
    if (updates.containsKey("modalityId")) {
        schedule.setModalityId(Long.valueOf(updates.get("modalityId").toString()));
    }
    if (updates.containsKey("disability")) {
        schedule.setDisability(Boolean.valueOf(updates.get("disability").toString()));
    }

    scheduleRepo.save(schedule);

    return RequestScheduleDTO.builder()
            .id(schedule.getId())
            .day(schedule.getDay())
            .startTime(schedule.getStartTime().toLocalTime().toString())
            .endTime(schedule.getEndTime().toLocalTime().toString())
            .classroomTypeId(schedule.getClassroomTypeId())
            .modalityId(schedule.getModalityId())
            .disability(schedule.getDisability())
            .build();
}




}
