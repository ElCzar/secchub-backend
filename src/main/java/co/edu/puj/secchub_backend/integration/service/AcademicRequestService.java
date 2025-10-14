package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.CombinedRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.IndividualRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.ProcessPlanningRequestDTO;
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
import java.util.HashMap;
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
    public Mono<List<AcademicRequestResponseDTO>> createAcademicRequestBatch(AcademicRequestBatchRequestDTO payload) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> Mono.fromCallable(() -> {
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
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Gets all academic requests for the current semester.
     * @return List of academic requests for the current semester
     */
    public List<AcademicRequestResponseDTO> findCurrentSemesterAcademicRequests() {
        Long currentSemesterId = semesterService.getCurrentSemesterId();
        return academicRequestRepository.findBySemesterId(currentSemesterId).stream()
                .map(this::mapToResponseDTO)
                .toList();
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
        // Manual mapping to avoid lazy loading issues
        AcademicRequestResponseDTO responseDTO = AcademicRequestResponseDTO.builder()
                .id(academicRequest.getId())
                .userId(academicRequest.getUserId())
                .courseId(academicRequest.getCourseId())
                .semesterId(academicRequest.getSemesterId())
                .startDate(academicRequest.getStartDate())
                .endDate(academicRequest.getEndDate())
                .capacity(academicRequest.getCapacity())
                .requestDate(academicRequest.getRequestDate())
                .observation(academicRequest.getObservation())
                // Agregar campos enriquecidos
                .userName(getUserName(academicRequest.getUserId()))
                .courseName(getCourseName(academicRequest.getCourseId()))
                .programName(getProgramName(academicRequest.getUserId()))
                .build();
        
        // Load schedules separately to avoid lazy loading issues
        List<RequestSchedule> schedules = requestScheduleRepository.findByAcademicRequestId(academicRequest.getId());
        
        List<RequestScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> {
                    // Manual mapping to ensure all fields are correctly transferred
                    RequestScheduleResponseDTO dto = RequestScheduleResponseDTO.builder()
                            .id(schedule.getId())
                            .academicRequestId(schedule.getAcademicRequestId())
                            .classRoomTypeId(schedule.getClassroomTypeId())  // Mapping field name correctly
                            .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                            .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                            .day(schedule.getDay())
                            .modalityId(schedule.getModalityId())
                            .disability(schedule.getDisability())
                            .build();
                    return dto;
                })
                .toList();
        
        responseDTO.setSchedules(scheduleDTOs);
        return responseDTO;
    }

    /**
     * Processes planning requests by creating classes from combined and individual academic requests.
     * This method handles the combinations selected by the user and creates corresponding classes.
     * @param processPlanningRequestDTO DTO containing combined and individual requests
     * @return Map with processing results
     */
    @Transactional
    public Mono<Map<String, Object>> processPlanningRequests(ProcessPlanningRequestDTO processPlanningRequestDTO) {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            List<String> createdClasses = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            try {
                // Procesar solicitudes combinadas
                if (processPlanningRequestDTO.getCombinedRequests() != null) {
                    for (CombinedRequestDTO combinedRequest : processPlanningRequestDTO.getCombinedRequests()) {
                        try {
                            String combinedObservation = createCombinedObservation(combinedRequest);
                            String className = String.join(" + ", combinedRequest.getMaterias());
                            
                            // Crear una entrada que represente la combinación
                            // Por ahora solo registramos la combinación - más tarde se puede implementar crear clases reales
                            createdClasses.add(String.format("Combinación: %s (Cupos: %d)", className, combinedRequest.getCupos()));
                            
                            // Actualizar las observaciones de las solicitudes originales para indicar que fueron combinadas
                            updateOriginalRequestsWithCombination(combinedRequest.getSourceIds(), combinedObservation);
                            
                        } catch (Exception e) {
                            errors.add("Error procesando combinación: " + e.getMessage());
                        }
                    }
                }

                // Procesar solicitudes individuales que no fueron eliminadas
                if (processPlanningRequestDTO.getIndividualRequests() != null) {
                    for (IndividualRequestDTO individualRequest : processPlanningRequestDTO.getIndividualRequests()) {
                        if (!"deleted".equals(individualRequest.getState())) {
                            try {
                                // Crear clase individual - por ahora solo registramos
                                createdClasses.add(String.format("Individual: %s - %s (Cupos: %d)", 
                                    individualRequest.getProgram(), individualRequest.getMateria(), individualRequest.getCupos()));
                            } catch (Exception e) {
                                errors.add("Error procesando solicitud individual: " + e.getMessage());
                            }
                        }
                    }
                }

                result.put("success", true);
                result.put("createdClasses", createdClasses);
                result.put("totalProcessed", createdClasses.size());
                result.put("errors", errors);
                
                return result;
                
            } catch (Exception e) {
                result.put("success", false);
                result.put("error", "Error general procesando solicitudes: " + e.getMessage());
                return result;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates the observation text for combined requests showing which programs were combined.
     */
    private String createCombinedObservation(CombinedRequestDTO combinedRequest) {
        StringBuilder observation = new StringBuilder("Combinación de programas: ");
        
        for (int i = 0; i < combinedRequest.getPrograms().size(); i++) {
            if (i > 0) observation.append(", ");
            observation.append(combinedRequest.getPrograms().get(i));
            // Aquí podríamos agregar los cupos específicos de cada programa si estuvieran disponibles
        }
        
        observation.append(String.format(" (Total cupos: %d)", combinedRequest.getCupos()));
        
        return observation.toString();
    }

    /**
     * Updates the original academic requests to indicate they were combined.
     */
    private void updateOriginalRequestsWithCombination(List<Long> sourceIds, String combinationNote) {
        if (sourceIds != null) {
            for (Long sourceId : sourceIds) {
                try {
                    var request = academicRequestRepository.findById(sourceId);
                    if (request.isPresent()) {
                        AcademicRequest academicRequest = request.get();
                        String currentObservation = academicRequest.getObservation() != null ? academicRequest.getObservation() : "";
                        academicRequest.setObservation(currentObservation + "\n" + combinationNote);
                        academicRequestRepository.save(academicRequest);
                    }
                } catch (Exception e) {
                    // Log error but continue processing
                    System.err.println("Error updating request " + sourceId + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates test data with schedules for debugging purposes
     */
    @Transactional
    public String createTestDataWithSchedules() {
        try {
            // Use a fixed semester ID for testing (assuming 1L exists)
            Long currentSemesterId = 1L;
            
            // Create first academic request
            AcademicRequest request1 = AcademicRequest.builder()
                    .userId(1L)
                    .courseId(1L)
                    .semesterId(currentSemesterId)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(4))
                    .capacity(15)
                    .observation("Solicitud de prueba para Redes - Sistemas")
                    .requestDate(LocalDate.now())
                    .build();
            
            AcademicRequest savedRequest1 = academicRequestRepository.save(request1);
            
            // Create schedules for request1
            RequestSchedule schedule1 = RequestSchedule.builder()
                    .academicRequestId(savedRequest1.getId())
                    .classroomTypeId(1L)
                    .startTime(java.sql.Time.valueOf("08:00:00"))
                    .endTime(java.sql.Time.valueOf("10:00:00"))
                    .day("LUNES")
                    .modalityId(1L)
                    .disability(false)
                    .build();
            
            RequestSchedule schedule2 = RequestSchedule.builder()
                    .academicRequestId(savedRequest1.getId())
                    .classroomTypeId(1L)
                    .startTime(java.sql.Time.valueOf("14:00:00"))
                    .endTime(java.sql.Time.valueOf("16:00:00"))
                    .day("MIERCOLES")
                    .modalityId(1L)
                    .disability(false)
                    .build();
            
            requestScheduleRepository.save(schedule1);
            requestScheduleRepository.save(schedule2);
            
            // Create second academic request
            AcademicRequest request2 = AcademicRequest.builder()
                    .userId(2L)
                    .courseId(1L)
                    .semesterId(currentSemesterId)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(4))
                    .capacity(10)
                    .observation("Solicitud de prueba para Redes - Electrónica")
                    .requestDate(LocalDate.now())
                    .build();
            
            AcademicRequest savedRequest2 = academicRequestRepository.save(request2);
            
            // Create schedules for request2
            RequestSchedule schedule3 = RequestSchedule.builder()
                    .academicRequestId(savedRequest2.getId())
                    .classroomTypeId(1L)
                    .startTime(java.sql.Time.valueOf("10:00:00"))
                    .endTime(java.sql.Time.valueOf("12:00:00"))
                    .day("MARTES")
                    .modalityId(1L)
                    .disability(false)
                    .build();
            
            RequestSchedule schedule4 = RequestSchedule.builder()
                    .academicRequestId(savedRequest2.getId())
                    .classroomTypeId(1L)
                    .startTime(java.sql.Time.valueOf("16:00:00"))
                    .endTime(java.sql.Time.valueOf("18:00:00"))
                    .day("JUEVES")
                    .modalityId(1L)
                    .disability(false)
                    .build();
            
            requestScheduleRepository.save(schedule3);
            requestScheduleRepository.save(schedule4);
            
            return "Created test data: 2 academic requests with schedules";
            
        } catch (Exception e) {
            System.err.println("Error creating test data: " + e.getMessage());
            return "Error creating test data: " + e.getMessage();
        }
    }

    /**
     * Método helper para obtener el nombre del usuario
     */
    private String getUserName(Long userId) {
        // Por ahora, usamos un mapeo básico basado en los IDs conocidos
        // En el futuro se puede mejorar con consultas reales a la BD
        if (userId == 10) {
            return "Programa Sistemas";
        }
        return "Usuario " + userId;
    }

    /**
     * Método helper para obtener el nombre del curso
     */
    private String getCourseName(Long courseId) {
        // Por ahora, usamos un mapeo básico basado en los IDs conocidos
        // En el futuro se puede mejorar con consultas reales a la BD
        switch (courseId.intValue()) {
            case 1: return "Database Systems";
            case 2: return "Software Engineering";
            case 3: return "Data Structures";
            default: return "Curso " + courseId;
        }
    }

    /**
     * Método helper para obtener el nombre del programa
     */
    private String getProgramName(Long userId) {
        // Por ahora, basado en el usuario conocido
        if (userId == 10) {
            return "Ingeniería de Sistemas";
        }
        return "Programa Desconocido";
    }
}