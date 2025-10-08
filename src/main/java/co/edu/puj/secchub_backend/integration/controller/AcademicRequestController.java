package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.integration.service.AcademicRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    private final AcademicRequestService academicRequestService;

    /**
     * Creates a batch of academic requests with schedules.
     * @param academicRequestBatchRequestDTO with batch request information
     * @return List of created academic requests
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<AcademicRequestResponseDTO>>> createAcademicRequestBatch(@RequestBody AcademicRequestBatchRequestDTO academicRequestBatchRequestDTO) {
        return Mono.fromCallable(() -> academicRequestService.createAcademicRequestBatch(academicRequestBatchRequestDTO))
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
     * Gets all academic requests for the current semester.
     * @return List of academic requests for the current semester
     */
    @GetMapping("/current-semester")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public List<AcademicRequestResponseDTO> getCurrentSemesterAcademicRequests() {
        return academicRequestService.findCurrentSemesterAcademicRequests();
    }

    /**
     * Gets all academic requests.
     * @return List of academic requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public List<AcademicRequestResponseDTO> getAllAcademicRequests() {
        return academicRequestService.findAllAcademicRequests();
    }

    /**
     * Gets an academic request by its ID.
     * @param requestId Request ID
     * @return Academic request found
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<AcademicRequestResponseDTO>> getAcademicRequestById(@PathVariable Long requestId) {
        return academicRequestService.findAcademicRequestById(requestId)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an academic request by its ID.
     * @param requestId Request ID
     * @param academicRequestRequestDTO with updated data
     * @return Updated academic request
     */
    @PutMapping("/{requestId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<AcademicRequestResponseDTO>> updateAcademicRequest(
            @PathVariable Long requestId,
            @RequestBody AcademicRequestRequestDTO academicRequestRequestDTO) {
        return academicRequestService.updateAcademicRequest(requestId, academicRequestRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * Adds a schedule to an academic request.
     * @param requestId Request ID
     * @param requestScheduleRequestDTO DTO with schedule data
     * @return Created schedule
     */
    @PostMapping("/{requestId}/schedules")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<RequestScheduleResponseDTO>> addRequestSchedule(
            @PathVariable Long requestId,
            @RequestBody RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return academicRequestService.addRequestSchedule(requestId, requestScheduleRequestDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    /**
     * Gets schedules associated with an academic request.
     * @param requestId Request ID
     * @return List of schedules
     */
    @GetMapping("/{requestId}/schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public List<RequestScheduleResponseDTO> getRequestSchedules(@PathVariable Long requestId) {
        return academicRequestService.findRequestSchedulesByAcademicRequestId(requestId);
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
     * @param requestScheduleRequestDTO with updated data
     * @return Updated schedule
     */
    @PutMapping("/{requestId}/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<RequestScheduleResponseDTO>> updateRequestSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return academicRequestService.updateRequestSchedule(scheduleId, requestScheduleRequestDTO)
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
    public Mono<ResponseEntity<RequestScheduleResponseDTO>> patchRequestSchedule(
            @PathVariable Long requestId,
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {
        return academicRequestService.patchRequestSchedule(scheduleId, updates)
                .map(ResponseEntity::ok);
    }
}
