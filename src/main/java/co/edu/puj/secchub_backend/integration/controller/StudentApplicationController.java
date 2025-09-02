package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.StudentApplicationDTO;
import co.edu.puj.secchub_backend.integration.model.Student;
import co.edu.puj.secchub_backend.integration.service.StudentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las solicitudes de monitoría de estudiantes.
 * Proporciona endpoints para crear, consultar, aprobar y rechazar solicitudes de monitoría.
 */
@RestController
@RequestMapping("/api/monitors/requests")
@RequiredArgsConstructor
public class StudentApplicationController {

    /** Servicio para la lógica de negocio de solicitudes de monitoría. */
    private final StudentApplicationService service;

    /**
     * Crea una nueva solicitud de monitoría.
     * @param dto DTO con los datos de la solicitud
     * @return Estudiante con la solicitud creada
     */
    @PostMapping
    public ResponseEntity<Student> createRequest(@RequestBody StudentApplicationDTO dto) {
        Student saved = service.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Obtiene todas las solicitudes de monitoría.
     * @return Lista de estudiantes con solicitudes
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllRequests() {
        return ResponseEntity.ok(service.listAll());
    }

    /**
     * Obtiene una solicitud de monitoría por su ID.
     * @param id ID de la solicitud
     * @return Estudiante con la solicitud encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Aprueba una solicitud de monitoría.
     * @param id ID de la solicitud
     * @param statusId ID del estado de aprobación
     * @return Respuesta sin contenido
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id, @RequestParam Long statusId) {
        service.approveRequest(id, statusId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechaza una solicitud de monitoría.
     * @param id ID de la solicitud
     * @param statusId ID del estado de rechazo
     * @return Respuesta sin contenido
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id, @RequestParam Long statusId) {
        service.rejectRequest(id, statusId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene solicitudes de monitoría por estado.
     * @param statusId ID del estado
     * @return Lista de estudiantes con solicitudes en ese estado
     */
    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<Student>> getRequestsByStatus(@PathVariable Long statusId) {
        return ResponseEntity.ok(service.listByStatus(statusId));
    }

    /**
     * Obtiene solicitudes de monitoría para una sección específica.
     * @param sectionId ID de la sección
     * @return Lista de estudiantes con solicitudes en esa sección
     */
    @GetMapping("/section/{sectionId}/requests")
    public ResponseEntity<List<Student>> getRequestsForSection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(service.listForSection(sectionId));
    }
}
