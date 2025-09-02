package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.RequestScheduleDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.service.AcademicRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar solicitudes académicas y sus horarios asociados.
 * Proporciona endpoints para crear, consultar, actualizar y eliminar solicitudes y horarios.
 */
@RestController
@RequestMapping("/api/academic-requests")
@RequiredArgsConstructor
public class AcademicRequestController {

    /** Servicio para la lógica de negocio de solicitudes académicas. */
    private final AcademicRequestService academicRequestService;

    /**
     * Crea un lote de solicitudes académicas con horarios.
     * @param payload DTO con la información del lote de solicitudes
     * @return Lista de solicitudes académicas creadas
     */
    @PostMapping
    public ResponseEntity<List<AcademicRequest>> createBatch(@RequestBody AcademicRequestBatchDTO payload) {
        List<AcademicRequest> saved = academicRequestService.createBatch(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Elimina una solicitud académica por su ID.
     * @param requestId ID de la solicitud a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long requestId) {
        academicRequestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene todas las solicitudes académicas.
     * @return Lista de solicitudes académicas
     */
    @GetMapping
    public ResponseEntity<List<AcademicRequest>> getAllRequests() {
        return ResponseEntity.ok(academicRequestService.findAll());
    }

    /**
     * Obtiene una solicitud académica por su ID.
     * @param id ID de la solicitud
     * @return Solicitud académica encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<AcademicRequest> getRequestById(@PathVariable Long id) {
        AcademicRequest request = academicRequestService.findById(id);
        return ResponseEntity.ok(request);
    }

    /**
     * Actualiza una solicitud académica por su ID.
     * @param id ID de la solicitud
     * @param dto DTO con los datos actualizados
     * @return Solicitud académica actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<AcademicRequest> updateRequest(
            @PathVariable Long id,
            @RequestBody AcademicRequestDTO dto) {
        AcademicRequest updated = academicRequestService.updateRequest(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Agrega un horario a una solicitud académica.
     * @param requestId ID de la solicitud
     * @param dto DTO con los datos del horario
     * @return Horario creado
     */
    @PostMapping("/{requestId}/schedules")
    public ResponseEntity<RequestScheduleDTO> addSchedule(
            @PathVariable Long requestId,
            @RequestBody RequestScheduleDTO dto) {
        RequestScheduleDTO saved = academicRequestService.addSchedule(requestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Obtiene los horarios asociados a una solicitud académica.
     * @param requestId ID de la solicitud
     * @return Lista de horarios
     */
    @GetMapping("/{requestId}/schedules")
    public ResponseEntity<List<RequestScheduleDTO>> getSchedules(@PathVariable Long requestId) {
        List<RequestScheduleDTO> schedules = academicRequestService.findSchedulesByRequest(requestId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Elimina un horario específico de una solicitud académica.
     * @param requestId ID de la solicitud
     * @param scheduleId ID del horario
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{requestId}/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId) {
        academicRequestService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza un horario específico de una solicitud académica.
     * @param requestId ID de la solicitud
     * @param scheduleId ID del horario
     * @param dto DTO con los datos actualizados
     * @return Horario actualizado
     */
    @PutMapping("/{requestId}/schedules/{scheduleId}")
    public ResponseEntity<RequestScheduleDTO> updateSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody RequestScheduleDTO dto) {
        RequestScheduleDTO updated = academicRequestService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Actualiza parcialmente un horario de una solicitud académica.
     * @param requestId ID de la solicitud
     * @param scheduleId ID del horario
     * @param updates Mapa con los campos a actualizar
     * @return Horario actualizado parcialmente
     */
    @PatchMapping("/{requestId}/schedules/{scheduleId}")
    public ResponseEntity<RequestScheduleDTO> patchSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {
        RequestScheduleDTO updated = academicRequestService.patchSchedule(scheduleId, updates);
        return ResponseEntity.ok(updated);
    }
}
