package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestBadRequest;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestNotFound;
import co.edu.puj.secchub_backend.integration.exception.RequestScheduleNotFound;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import co.edu.puj.secchub_backend.integration.repository.AcademicRequestRepository;
import co.edu.puj.secchub_backend.integration.repository.RequestScheduleRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcademicRequestService {
    private final ModelMapper modelMapper;
    private final AcademicRequestRepository academicRequestRepository;
    private final RequestScheduleRepository requestScheduleRepository;
    private final SecurityModuleUserContract userService;
    private final AdminModuleSemesterContract semesterService;

    /**
     * Creates a batch of academic requests with their associated schedules.
     * @param academicRequestBatchRequestDTO with batch request information
     * @return List of created academic requests
     */
    @Transactional
    public List<AcademicRequestResponseDTO> createAcademicRequestBatch(AcademicRequestBatchRequestDTO payload) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    String userEmail = securityContext.getAuthentication().getName();
                    Long userId = userService.getUserIdByEmail(userEmail);

                    Long currentSemesterId = semesterService.getCurrentSemesterId();

                    List<AcademicRequestResponseDTO> createdRequests = new ArrayList<>();
                    for (AcademicRequestRequestDTO item : payload.getRequests()) {
                        AcademicRequest academicRequest = modelMapper.map(item, AcademicRequest.class);
                        academicRequest.setUserId(userId);
                        academicRequest.setSemesterId(currentSemesterId);
                        academicRequest.setRequestDate(LocalDate.now());
                        academicRequest.setSchedules(null);
                        
                        AcademicRequest saved = academicRequestRepository.save(academicRequest);

                        if (item.getSchedules() == null) {
                            throw new AcademicRequestBadRequest("Each academic request must have at least one schedule");
                        }

                        for (RequestScheduleRequestDTO schedule : item.getSchedules()) {
                            RequestSchedule requestSchedule = modelMapper.map(schedule, RequestSchedule.class);
                            requestSchedule.setAcademicRequestId(saved.getId());
                            requestScheduleRepository.save(requestSchedule);
                        }
                        
                        createdRequests.add(mapToResponseDTO(saved));
                    }

                    return createdRequests;
                })
                .block();
    }

    /**
     * Gets all academic requests
     * @return List of academic requests
     */
    public List<AcademicRequestResponseDTO> findAllAcademicRequests() {
        return academicRequestRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets an academic request by ID.
     * @param requestId Request ID
     * @return Academic request found
     */
    public Mono<AcademicRequestResponseDTO> findAcademicRequestById(Long requestId) {
        return Mono.fromCallable(() -> academicRequestRepository.findById(requestId)
                .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for consult: " + requestId)))
                .map(this::mapToResponseDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes an academic request by ID.
     * @param requestId Request ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteAcademicRequest(Long requestId) {
        return Mono.fromCallable(() -> {
            if (!academicRequestRepository.existsById(requestId)) {
                throw new AcademicRequestNotFound("AcademicRequest not found for deletion: " + requestId);
            }
            academicRequestRepository.deleteById(requestId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Updates an academic request.
     * @param requestId Request ID
     * @param academicRequestRequestDTO with updated data
     * @return Updated academic request
     */
    public Mono<AcademicRequestResponseDTO> updateAcademicRequest(Long requestId, AcademicRequestRequestDTO academicRequestRequestDTO) {
        return Mono.fromCallable(() -> {
            AcademicRequest request = academicRequestRepository.findById(requestId)
                    .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for update: " + requestId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(academicRequestRequestDTO, request);
            AcademicRequest savedRequest = academicRequestRepository.save(request);
            return mapToResponseDTO(savedRequest);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Adds a schedule to an academic request.
     * @param requestId Request ID
     * @param requestScheduleRequestDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<RequestScheduleResponseDTO> addRequestSchedule(Long requestId, RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            academicRequestRepository.findById(requestId)
                .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for schedule creation: " + requestId));

            RequestSchedule schedule = modelMapper.map(requestScheduleRequestDTO, RequestSchedule.class);
            schedule.setAcademicRequestId(requestId);

            RequestSchedule savedSchedule = requestScheduleRepository.save(schedule);

            return modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules associated with a request.
     * @param requestId Request ID
     * @return List of schedules
     */
    public List<RequestScheduleResponseDTO> findRequestSchedulesByAcademicRequestId(Long requestId) {
        academicRequestRepository.findById(requestId)
                .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for schedule retrieval: " + requestId));

        List<RequestSchedule> requestSchedules = requestScheduleRepository.findByAcademicRequestId(requestId);
        return requestSchedules.stream()
                .map(requestSchedule -> modelMapper.map(requestSchedule, RequestScheduleResponseDTO.class))
                .toList();
    }

    /**
     * Updates a specific schedule.
     * @param scheduleId Schedule ID
     * @param requestScheduleRequestDTO DTO with updated data
     */
    public Mono<RequestScheduleResponseDTO> updateRequestSchedule(Long scheduleId, RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            RequestSchedule schedule = requestScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RequestScheduleNotFound("RequestSchedule not found for update: " + scheduleId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(requestScheduleRequestDTO, schedule);

            RequestSchedule savedSchedule = requestScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a schedule by ID.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteRequestSchedule(Long scheduleId) {
        return Mono.fromCallable(() -> {
            if (!requestScheduleRepository.existsById(scheduleId)) {
                throw new RequestScheduleNotFound("RequestSchedule not found for deletion: " + scheduleId);
            }
            requestScheduleRepository.deleteById(scheduleId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Partially updates a schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Updated schedule
     */
    public Mono<RequestScheduleResponseDTO> patchRequestSchedule(Long scheduleId, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            RequestSchedule schedule = requestScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RequestScheduleNotFound("RequestSchedule not found for partial update: " + scheduleId));

            RequestScheduleResponseDTO updateDTO = new RequestScheduleResponseDTO();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "startTime" -> updateDTO.setStartTime((String) value);
                    case "endTime" -> updateDTO.setEndTime((String) value);
                    case "day" -> updateDTO.setDay((String) value);
                    case "classRoomTypeId" -> updateDTO.setClassRoomTypeId((Long) value);
                    case "modalityId" -> updateDTO.setModalityId((Long) value);
                    case "disability" -> updateDTO.setDisability((Boolean) value);
                    default -> {
                        // Ignore unknown fields
                    }
                }
            });

            modelMapper.map(updateDTO, schedule);
            RequestSchedule savedSchedule = requestScheduleRepository.save(schedule);
            return modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ======================
    // Private utilities
    // ======================
    
    private AcademicRequestResponseDTO mapToResponseDTO(AcademicRequest academicRequest) {
        AcademicRequestResponseDTO responseDTO = modelMapper.map(academicRequest, AcademicRequestResponseDTO.class);
        
        // Load schedules for this request
        List<RequestSchedule> schedules = requestScheduleRepository.findByAcademicRequestId(academicRequest.getId());
        List<RequestScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> modelMapper.map(schedule, RequestScheduleResponseDTO.class))
                .toList();
        
        responseDTO.setSchedules(scheduleDTOs);
        return responseDTO;
    }
}