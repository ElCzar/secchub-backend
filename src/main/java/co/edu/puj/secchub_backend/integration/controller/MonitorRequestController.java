package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.MonitorRequestDTO;
import co.edu.puj.secchub_backend.integration.model.MonitorRequest;
import co.edu.puj.secchub_backend.integration.service.MonitorRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Monitor Requests.
 */
@RestController
@RequestMapping("/api/monitors/requests")
@RequiredArgsConstructor
public class MonitorRequestController {

    private final MonitorRequestService monitorRequestService;

    /**
     * Create a new monitor request.
     */
    @PostMapping
    public ResponseEntity<MonitorRequest> createRequest(@RequestBody MonitorRequestDTO dto) {
        MonitorRequest saved = monitorRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * List all requests.
     */
    @GetMapping
    public ResponseEntity<List<MonitorRequest>> getAllRequests() {
        return ResponseEntity.ok(monitorRequestService.listAll());
    }

    /**
     * Approve a request.
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id, @RequestParam Long statusId) {
        monitorRequestService.approveRequest(id, statusId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reject a request.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id, @RequestParam Long statusId) {
        monitorRequestService.rejectRequest(id, statusId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get one monitor request by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MonitorRequest> getRequestById(@PathVariable Long id) {
        MonitorRequest request = monitorRequestService.findById(id);
        return ResponseEntity.ok(request);
    }

    /**
     * Get all monitor requests by status.
     */
    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<MonitorRequest>> getRequestsByStatus(@PathVariable Long statusId) {
        List<MonitorRequest> requests = monitorRequestService.listByStatus(statusId);
        return ResponseEntity.ok(requests);
    }



    @GetMapping("/section/{sectionId}/requests")
    public ResponseEntity<List<MonitorRequest>> getRequestsForSection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(monitorRequestService.listForSection(sectionId));
    }






}
