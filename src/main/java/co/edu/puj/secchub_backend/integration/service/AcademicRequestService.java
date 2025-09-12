package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleDTO;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestNotFound;
import co.edu.puj.secchub_backend.integration.exception.RequestScheduleNotFound;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import co.edu.puj.secchub_backend.integration.repository.AcademicRequestRepository;
import co.edu.puj.secchub_backend.integration.repository.RequestScheduleRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AcademicRequestService {
    private final ModelMapper modelMapper;

    private final AcademicRequestRepository academicRequestRepository;
    private final RequestScheduleRepository requestScheduleRepository;

    // TODO: Add ValidationService, PdfGenerator, and EmailService dependencies if needed in future.

    /**
     * Creates a batch of academic requests with their associated schedules.
     * @param academicRequestBatchDTO with batch request information
     * @return List of created academic requests
     */
    @Transactional
    public List<AcademicRequest> createAcademicRequestBatch(AcademicRequestBatchDTO payload) {
        List<AcademicRequest> createdRequests = new ArrayList<>();
        for (AcademicRequestDTO item : payload.getRequests()) {
            AcademicRequest academicRequest = modelMapper.map(item, AcademicRequest.class);
            academicRequest.setSchedules(null);
            AcademicRequest saved = academicRequestRepository.save(academicRequest);

            if (item.getSchedules() != null) {
                List<RequestSchedule> savedSchedules = new ArrayList<>();
                for (RequestScheduleDTO schedule : item.getSchedules()) {
                    RequestSchedule requestSchedule = modelMapper.map(schedule, RequestSchedule.class);
                    requestSchedule.setAcademicRequestId(saved.getId());
                    RequestSchedule savedSchedule = requestScheduleRepository.save(requestSchedule);
                    savedSchedules.add(savedSchedule);
                }
                saved.setSchedules(savedSchedules);
            }
            createdRequests.add(saved);
        }

        return createdRequests;
    }

    /**
     * Gets all academic requests
     * @return Stream of academic requests
     */
    public Flux<AcademicRequest> findAllAcademicRequests() {
        return Mono.fromCallable(academicRequestRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets an academic request by ID.
     * @param requestId Request ID
     * @return Academic request found
     */
    public Mono<AcademicRequest> findAcademicRequestById(Long requestId) {
        return Mono.fromCallable(() -> academicRequestRepository.findById(requestId)
                .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for consult: " + requestId)))
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
                throw new AcademicRequestNotFound("AcademicRequest not found for delete: " + requestId);
            }
            academicRequestRepository.deleteById(requestId);
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Updates an academic request.
     * @param requestId Request ID
     * @param academicRequestDTO with updated data
     * @return Updated academic request
     */
    public Mono<AcademicRequest> updateAcademicRequest(Long requestId, AcademicRequestDTO academicRequestDTO) {
        return Mono.fromCallable(() -> {
            AcademicRequest request = academicRequestRepository.findById(requestId)
                    .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for update: " + requestId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(academicRequestDTO, request);
            return academicRequestRepository.save(request);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Adds a schedule to an academic request.
     * @param requestId Request ID
     * @param requestScheduleDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<RequestScheduleDTO> addRequestSchedule(Long requestId, RequestScheduleDTO requestScheduleDTO) {
        return Mono.fromCallable(() -> {
            academicRequestRepository.findById(requestId)
                .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for schedule creation: " + requestId));

            RequestSchedule schedule = modelMapper.map(requestScheduleDTO, RequestSchedule.class);

            RequestSchedule savedSchedule = requestScheduleRepository.save(schedule);

            requestScheduleDTO.setId(savedSchedule.getId());
            return requestScheduleDTO;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets schedules associated with a request.
     * @param requestId Request ID
     * @return Stream of schedules
     */
    public Flux<RequestScheduleDTO> findRequestSchedulesByAcademicRequestId(Long requestId) {
        return Mono.fromCallable(() -> {
            academicRequestRepository.findById(requestId)
                    .orElseThrow(() -> new AcademicRequestNotFound("AcademicRequest not found for schedule retrieval: " + requestId));

            List<RequestSchedule> requestSchedules = requestScheduleRepository.findByAcademicRequestId(requestId);
            List<RequestScheduleDTO> requestScheduleDTOs = new ArrayList<>();

            for (RequestSchedule requestSchedule : requestSchedules) {
                RequestScheduleDTO requestScheduleDTO = modelMapper.map(requestSchedule, RequestScheduleDTO.class);
                requestScheduleDTOs.add(requestScheduleDTO);
            }
            return requestScheduleDTOs;
        }).flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates a specific schedule.
     * @param scheduleId Schedule ID
     * @param requestScheduleDTO DTO with updated data
     */
    public Mono<RequestScheduleDTO> updateRequestSchedule(Long scheduleId, RequestScheduleDTO requestScheduleDTO) {
        return Mono.fromCallable(() -> {
            RequestSchedule schedule = requestScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RequestScheduleNotFound("RequestSchedule not found for update: " + scheduleId));

            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(requestScheduleDTO, schedule);

            requestScheduleRepository.save(schedule);

            return modelMapper.map(schedule, RequestScheduleDTO.class);
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
                throw new RequestScheduleNotFound("RequestSchedule not found: " + scheduleId);
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
    public Mono<RequestScheduleDTO> patchRequestSchedule(Long scheduleId, Map<String, Object> updates) {
        return Mono.fromCallable(() -> {
            RequestSchedule schedule = requestScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RequestScheduleNotFound("RequestSchedule not found: " + scheduleId));
            
            RequestScheduleDTO updateDTO = new RequestScheduleDTO();
            modelMapper.map(updates, updateDTO);
            
            modelMapper.getConfiguration().setPropertyCondition(context -> 
                context.getSource() != null);
            modelMapper.map(updateDTO, schedule);

            requestScheduleRepository.save(schedule);

            return modelMapper.map(schedule, RequestScheduleDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ======================
    // Private utilities
    // ======================
    public int calculateWeeks(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        long days = ChronoUnit.DAYS.between(start, end.plusDays(1));
        return (int) Math.ceil(days / 7.0);
    }
}
