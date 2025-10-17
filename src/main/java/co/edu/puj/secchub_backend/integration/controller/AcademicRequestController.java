package co.edu.puj.secchub_backend.integration.controller;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.ProcessPlanningRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.integration.service.AcademicRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    private final AdminModuleSemesterContract semesterService;

    /**
     * Creates a batch of academic requests with schedules.
     * @param academicRequestBatchRequestDTO with batch request information
     * @return List of created academic requests
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROGRAM')")
    public Mono<ResponseEntity<List<AcademicRequestResponseDTO>>> createAcademicRequestBatch(@RequestBody AcademicRequestBatchRequestDTO academicRequestBatchRequestDTO) {
        return academicRequestService.createAcademicRequestBatch(academicRequestBatchRequestDTO)
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
    public Mono<List<AcademicRequestResponseDTO>> getCurrentSemesterAcademicRequests() {
        System.out.println("🔵 [getCurrentSemesterAcademicRequests] Endpoint called");
        return Mono.fromCallable(() -> {
            List<AcademicRequestResponseDTO> requests = academicRequestService.findCurrentSemesterAcademicRequests();
            System.out.println("🔵 [getCurrentSemesterAcademicRequests] Returning " + requests.size() + " requests");
            requests.forEach(r -> System.out.println("   → Request ID: " + r.getId()));
            return requests;
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets all academic requests.
     * @return List of academic requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<List<AcademicRequestResponseDTO>> getAllAcademicRequests() {
        return Mono.fromCallable(() -> academicRequestService.findAllAcademicRequests())
                .subscribeOn(Schedulers.boundedElastic());
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
    public Mono<List<RequestScheduleResponseDTO>> getRequestSchedules(@PathVariable Long requestId) {
        return Mono.fromCallable(() -> academicRequestService.findRequestSchedulesByAcademicRequestId(requestId))
                .subscribeOn(Schedulers.boundedElastic());
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

    /**
     * Processes planning requests by creating classes from combined and individual academic requests.
     * @param processPlanningRequestDTO DTO containing combined and individual requests
     * @return Response indicating success
     */
    @PostMapping("/process-planning")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Map<String, Object>>> processPlanningRequests(
            @RequestBody ProcessPlanningRequestDTO processPlanningRequestDTO) {
        return academicRequestService.processPlanningRequests(processPlanningRequestDTO)
                .map(result -> ResponseEntity.ok(result));
    }

    /**
     * Temporary debugging endpoint to check schedules
     */
    @GetMapping("/debug-schedules")
    public Mono<ResponseEntity<Map<String, Object>>> debugSchedules() {
        return Mono.fromCallable(() -> academicRequestService.findCurrentSemesterAcademicRequests())
                .map(requests -> {
                    System.out.println("🐛 DEBUG: Total requests found: " + requests.size());
                    requests.forEach(request -> {
                        System.out.println("🐛 Request ID: " + request.getId() + " has " + 
                                (request.getSchedules() != null ? request.getSchedules().size() : 0) + " schedules");
                        if (request.getSchedules() != null) {
                            request.getSchedules().forEach(schedule -> {
                                System.out.println("  📅 Schedule: " + schedule.getDay() + " " + 
                                        schedule.getStartTime() + "-" + schedule.getEndTime());
                            });
                        }
                    });
                    return ResponseEntity.ok(Map.of("message", "Check console for debug info", "requests", requests));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * DEBUG: Check which semester is marked as current and return all academic requests grouped by semester
     */
    @GetMapping("/debug-current-semester")
    public Mono<ResponseEntity<Map<String, Object>>> debugCurrentSemester() {
        return Mono.fromCallable(() -> {
            Map<String, Object> debug = new java.util.HashMap<>();
            
            try {
                // Get current semester ID
                Long currentSemesterId = semesterService.getCurrentSemesterId();
                debug.put("currentSemesterId", currentSemesterId);
                
                // Get current semester requests
                List<AcademicRequestResponseDTO> currentSemesterRequests = 
                    academicRequestService.findCurrentSemesterAcademicRequests();
                debug.put("currentSemesterRequests", currentSemesterRequests);
                debug.put("currentSemesterRequestCount", currentSemesterRequests.size());
                
                // Get all requests
                List<AcademicRequestResponseDTO> allRequests = 
                    academicRequestService.findAllAcademicRequests();
                debug.put("allRequests", allRequests);
                debug.put("allRequestsCount", allRequests.size());
                
                // Group by semester ID
                Map<Long, java.util.List<AcademicRequestResponseDTO>> groupedBySemester = 
                    allRequests.stream()
                        .collect(java.util.stream.Collectors.groupingBy(AcademicRequestResponseDTO::getSemesterId));
                debug.put("groupedBySemester", groupedBySemester);
                
                System.out.println("🐛 DEBUG CURRENT SEMESTER:");
                System.out.println("   Current Semester ID: " + currentSemesterId);
                System.out.println("   Current Semester Requests: " + currentSemesterRequests.size());
                System.out.println("   All Requests: " + allRequests.size());
                groupedBySemester.forEach((semesterId, requests) -> {
                    System.out.println("   Semester " + semesterId + ": " + requests.size() + " requests");
                    requests.forEach(r -> System.out.println("      - Request ID: " + r.getId()));
                });
                
            } catch (Exception e) {
                debug.put("error", e.getMessage());
                e.printStackTrace();
            }
            
            return ResponseEntity.ok(debug);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Temporary endpoint to create test data with schedules
     */
    @PostMapping("/create-test-data")
    public Mono<ResponseEntity<Map<String, Object>>> createTestData() {
        return Mono.fromCallable(() -> academicRequestService.createTestDataWithSchedules())
                .map(result -> ResponseEntity.ok(Map.<String, Object>of("message", "Test data created", "result", result)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
