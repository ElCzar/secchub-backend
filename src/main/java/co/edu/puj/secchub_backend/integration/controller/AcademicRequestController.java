package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.RequestScheduleDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.service.AcademicRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing academic requests and their associated schedules.
 * Provides endpoints to create, query, update and delete requests and schedules.
 */
@RestController
@RequestMapping("/academic-requests")
@RequiredArgsConstructor
public class AcademicRequestController {

    /** Service for academic request business logic. */
    private final AcademicRequestService academicRequestService;

    /**
     * Creates a batch of academic requests with schedules.
     * @param academicRequestBatchDTO with batch request information
     * @return Stream of created academic requests
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<AcademicRequest>>> createAcademicRequestBatch(@RequestBody AcademicRequestBatchDTO academicRequestBatchDTO) {
        return Mono.fromCallable(() -> academicRequestService.createAcademicRequestBatch(academicRequestBatchDTO))
                .map(createdRequests -> ResponseEntity.status(HttpStatus.CREATED).body(createdRequests));
    }

    /**
     * Deletes an academic request by its ID.
     * @param requestId ID of the request to delete
     * @return Empty response with no content code 204
     */
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteAcademicRequest(@PathVariable Long requestId) {
        return academicRequestService.deleteAcademicRequest(requestId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Gets all academic requests.
     * @return Stream of academic requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<AcademicRequest>> getAllAcademicRequests() {
        return ResponseEntity.ok(academicRequestService.findAllAcademicRequests());
    }

    /**
     * Gets an academic request by its ID.
     * @param requestId Request ID
     * @return Academic request found
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<AcademicRequest>> getAcademicRequestById(@PathVariable Long requestId) {
        return academicRequestService.findAcademicRequestById(requestId)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an academic request by its ID.
     * @param requestId Request ID
     * @param academicRequestDTO with updated data
     * @return Updated academic request
     */
    @PutMapping("/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<AcademicRequest>> updateAcademicRequest(
            @PathVariable Long requestId,
            @RequestBody AcademicRequestDTO academicRequestDTO) {
        return academicRequestService.updateAcademicRequest(requestId, academicRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Adds a schedule to an academic request.
     * @param requestId Request ID
     * @param requestScheduleDTO DTO with schedule data
     * @return Created schedule
     */
    @PostMapping("/{requestId}/schedules")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<RequestScheduleDTO>> addRequestSchedule(
            @PathVariable Long requestId,
            @RequestBody RequestScheduleDTO requestScheduleDTO) {
        return academicRequestService.addRequestSchedule(requestId, requestScheduleDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets schedules associated with an academic request.
     * @param requestId Request ID
     * @return Stream of schedules
     */
    @GetMapping("/{requestId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<Flux<RequestScheduleDTO>> getRequestSchedules(@PathVariable Long requestId) {
        return ResponseEntity.ok(academicRequestService.findRequestSchedulesByAcademicRequestId(requestId));
    }

    /**
     * Deletes a specific schedule from an academic request.
     * @param requestId Request ID
     * @param scheduleId Schedule ID
     * @return Response with no content
     */
    @DeleteMapping("/{requestId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteRequestSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId) {
        return academicRequestService.deleteRequestSchedule(scheduleId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Updates a specific schedule of an academic request.
     * @param requestId Request ID
     * @param scheduleId Schedule ID
     * @param requestScheduleDTO with updated data
     * @return Updated schedule
     */
    @PutMapping("/{requestId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<RequestScheduleDTO>> updateRequestSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody RequestScheduleDTO requestScheduleDTO) {
        return academicRequestService.updateRequestSchedule(scheduleId, requestScheduleDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Partially updates a schedule of an academic request.
     * @param requestId Request ID
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Partially updated schedule
     */
    @PatchMapping("/{requestId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<RequestScheduleDTO>> patchRequestSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {
        return academicRequestService.patchRequestSchedule(scheduleId, updates)
                .map(ResponseEntity::ok);
    }
}
