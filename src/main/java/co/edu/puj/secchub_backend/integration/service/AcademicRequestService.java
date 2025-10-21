package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
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
import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
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
    private final AdminModuleCourseContract courseService;

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
     * Gets all academic requests for a specific semester filtered by the authenticated user.
     * @param semesterId The semester ID to filter requests
     * @return List of academic requests for the specified semester and user
     */
    public Mono<List<AcademicRequestResponseDTO>> findAcademicRequestsBySemesterAndUser(Long semesterId) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    String userEmail = securityContext.getAuthentication().getName();
                    Long userId = userService.getUserIdByEmail(userEmail);
                    
                    List<AcademicRequestResponseDTO> requests = academicRequestRepository
                            .findBySemesterIdAndUserId(semesterId, userId).stream()
                            .map(this::mapToResponseDTO)
                            .toList();
                    
                    return Mono.just(requests);
                });
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
                            .classRoomTypeId(schedule.getClassRoomTypeId())
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
                    // Error updating request, continuing with next request
                }
            }
        }
    }

  
    /**
     * Método helper para obtener el nombre del usuario
     */
    private String getUserName(Long userId) {

        return "Usuario " + userId;
    }

    /**
     * Método helper para obtener el nombre del curso
     */
    private String getCourseName(Long courseId) {
        try {
            // Delegar al servicio de cursos (implementación real que consulta la BD)
            String name = courseService.getCourseName(courseId);
            return name != null && !name.trim().isEmpty() ? name : "Curso " + courseId;
        } catch (Exception e) {
            // Fallback seguro si algo falla en la capa de cursos
            return "Curso " + courseId;
        }
    }

    /**
     * Gets the current user context including user name and current semester info.
     * @return Map containing user context information
     */
    public Mono<Map<String, Object>> getCurrentUserContext() {
        System.out.println("🔍 AcademicRequestService: Iniciando obtención de contexto del usuario");
        
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    System.out.println("🔐 AcademicRequestService: SecurityContext obtenido");
                    String userEmail = securityContext.getAuthentication().getName();
                    System.out.println("📧 AcademicRequestService: Email del usuario autenticado: " + userEmail);
                    
                    return this.getUserContextByEmail(userEmail);
                })
                .onErrorResume(error -> {
                    System.err.println("❌ AcademicRequestService: No hay usuario autenticado: " + error.getMessage());
                    System.out.println("🔄 AcademicRequestService: Usando usuario por defecto (program@secchub.com)");
                    
                    // Usar usuario por defecto cuando no hay autenticación
                    return this.getUserContextByEmail("program@secchub.com");
                });
    }
    
    /**
     * Gets user context by email (helper method).
     */
    private Mono<Map<String, Object>> getUserContextByEmail(String userEmail) {
        return Mono.fromCallable(() -> {
            Long userId = userService.getUserIdByEmail(userEmail);
            System.out.println("🆔 AcademicRequestService: ID del usuario: " + userId);
            
            // Obtener el nombre del usuario programa directamente
            String programUserName = getUserNameByEmail(userEmail);
            System.out.println("� AcademicRequestService: Nombre del usuario programa: " + programUserName);
            
            return new Object[]{userId, programUserName};
        }).flatMap(userData -> {
            Object[] data = (Object[]) userData;
            Long userId = (Long) data[0];
            String programUserName = (String) data[1];
            
            return semesterService.getCurrentSemester()
                    .map(semester -> {
                        System.out.println("📅 AcademicRequestService: Semestre actual: " + semester);
                        
                        Map<String, Object> context = new HashMap<>();
                        context.put("careerId", "PROG" + userId);
                        context.put("careerName", programUserName); // Aquí va el nombre del usuario programa
                        context.put("semester", semester.getYear() + "-" + semester.getPeriod());
                        context.put("semesterId", semester.getId());
                        
                        System.out.println("✅ AcademicRequestService: Contexto creado: " + context);
                        return context;
                    })
                    .doOnError(error -> {
                        System.err.println("❌ AcademicRequestService: Error obteniendo semestre: " + error.getMessage());
                        error.printStackTrace();
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Método helper para obtener el nombre del usuario por email
     */
    private String getUserNameByEmail(String userEmail) {
        try {
            // Obtener el nombre real del usuario de la base de datos usando el UserService
            UserInformationResponseDTO userInfo = userService.getUserInformationByEmail(userEmail).block();
            
            if (userInfo != null) {
                // Construir el nombre completo usando los datos reales de la BD
                String fullName = "";
                if (userInfo.getName() != null && !userInfo.getName().trim().isEmpty()) {
                    fullName += userInfo.getName().trim();
                }
                if (userInfo.getLastName() != null && !userInfo.getLastName().trim().isEmpty()) {
                    if (!fullName.isEmpty()) {
                        fullName += " ";
                    }
                    fullName += userInfo.getLastName().trim();
                }
                
                // Si no hay nombre completo disponible, usar el username
                if (fullName.trim().isEmpty()) {
                    fullName = userInfo.getUsername() != null ? userInfo.getUsername() : "Usuario sin nombre";
                }
                
                return fullName;
            } else {
                throw new RuntimeException("No se encontró información del usuario");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error obteniendo nombre de usuario: " + e.getMessage());
            throw new RuntimeException("No se pudo obtener el nombre del usuario para email: " + userEmail, e);
        }
    }    /**
     * Método helper para obtener el nombre del programa
     */
    private String getProgramName(Long userId) {

        
        return "Programa Desconocido";
    }
}