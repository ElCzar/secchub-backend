package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.CombinedRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.ProcessPlanningRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestBadRequest;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestNotFound;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestServerErrorException;
import co.edu.puj.secchub_backend.integration.exception.RequestScheduleNotFound;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import co.edu.puj.secchub_backend.integration.repository.AcademicRequestRepository;
import co.edu.puj.secchub_backend.integration.repository.RequestScheduleRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcademicRequestService {
    private final ModelMapper modelMapper;
    private final TransactionalOperator transactionalOperator;
    
    private final AcademicRequestRepository academicRequestRepository;
    private final RequestScheduleRepository requestScheduleRepository;

    private final SecurityModuleUserContract userService;
    private final AdminModuleSemesterContract semesterService;
    private final AdminModuleCourseContract courseService;
    private final AdminModuleSectionContract sectionService;

    /**
     * Creates a batch of academic requests with their associated schedules.
     * @param academicRequestBatchRequestDTO with batch request information
     * @return Flux of created academic requests
     */
    public Flux<AcademicRequestResponseDTO> createAcademicRequestBatch(AcademicRequestBatchRequestDTO payload) {
        return semesterService.getCurrentSemesterId()
        .flatMap(currentSemesterId -> this.getCurrentUserId()
            .map(userId -> Map.entry(userId, currentSemesterId)))
        .flatMapMany(entry -> {
            Long userId = entry.getKey();
            Long currentSemesterId = entry.getValue();
            
            return Flux.fromIterable(payload.getRequests())
                .flatMap(requestDTO -> {
                    AcademicRequest academicRequest = modelMapper.map(requestDTO, AcademicRequest.class);
                    academicRequest.setUserId(userId);
                    academicRequest.setSemesterId(currentSemesterId);
                    academicRequest.setRequestDate(LocalDate.now());

                    return academicRequestRepository.save(academicRequest)
                        .flatMap(savedRequest -> {
                            if (requestDTO.getSchedules() == null || requestDTO.getSchedules().isEmpty()) {
                                return mapToResponseDTO(savedRequest);
                            }
                            
                            return Flux.fromIterable(requestDTO.getSchedules())
                                .flatMap(scheduleDTO -> {
                                    RequestSchedule schedule = modelMapper.map(scheduleDTO, RequestSchedule.class);
                                    schedule.setAcademicRequestId(savedRequest.getId());
                                    return requestScheduleRepository.save(schedule);
                                })
                                .collectList()
                                .flatMap(schedules -> mapToResponseDTO(savedRequest, schedules));
                        });
                });
        })
        .as(transactionalOperator::transactional)
        .onErrorMap(ex -> {
            if (ex instanceof AcademicRequestBadRequest) {
                return ex;
            }
            return new AcademicRequestServerErrorException("Error creating academic request batch: " + ex.getMessage());
        });
    }

    /**
     * Gets all academic requests for the current semester.
     * @return List of academic requests for the current semester
     */
    public Flux<AcademicRequestResponseDTO> findCurrentSemesterAcademicRequests() {
        return semesterService.getCurrentSemesterId()
            .flatMapMany(currentSemesterId -> 
                academicRequestRepository.findBySemesterId(currentSemesterId)
                    .filterWhen(this::filterByUserRole)
                    .flatMap(this::getClassSchedulesForRequest)
            );
    }

    /**
     * Gets all academic requests for a specific semester filtered by the authenticated user.
     * @param semesterId The semester ID to filter requests
     * @return List of academic requests for the specified semester and user
     */
    public Flux<AcademicRequestResponseDTO> findAcademicRequestsBySemesterAndUser(Long semesterId) {
        return academicRequestRepository.findBySemesterId(semesterId)
            .filterWhen(this::filterByUserRole)
            .flatMap(this::getClassSchedulesForRequest);
    }

    /**
     * Gets all academic requests
     * @return List of academic requests
     */
    public Flux<AcademicRequestResponseDTO> findAllAcademicRequests() {
        return academicRequestRepository.findAll()
            .filterWhen(this::filterByUserRole)
            .flatMap(this::getClassSchedulesForRequest);
    }

    /**
     * Gets an academic request by ID.
     * @param requestId Request ID
     * @return Academic request found
     */
    public Mono<AcademicRequestResponseDTO> findAcademicRequestById(Long requestId) {
        return academicRequestRepository.findById(requestId)
            .filterWhen(this::filterByUserRole)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("AcademicRequest not found: " + requestId)))
            .flatMap(this::getClassSchedulesForRequest);
    }

    /**
     * Deletes an academic request by ID.
     * @param requestId Request ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteAcademicRequest(Long requestId) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("AcademicRequest not found for deletion: " + requestId)))
            .flatMap(request -> academicRequestRepository.deleteById(requestId))
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error deleting academic request: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to delete academic request");
            });
    }

    /**
     * Updates an academic request.
     * @param requestId Request ID
     * @param academicRequestRequestDTO with updated data
     * @return Updated academic request
     */
    public Mono<AcademicRequestResponseDTO> updateAcademicRequest(Long requestId, AcademicRequestRequestDTO academicRequestRequestDTO) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("AcademicRequest not found for update: " + requestId)))
            .flatMap(request -> {
                modelMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);
                modelMapper.map(academicRequestRequestDTO, request);
                return academicRequestRepository.save(request);
            })
            .flatMap(this::mapToResponseDTO)
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error updating academic request: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to update academic request");
            });
    }

    /**
     * Adds a schedule to an academic request.
     * @param requestId Request ID
     * @param requestScheduleRequestDTO DTO with schedule data
     * @return Created schedule DTO
     */
    public Mono<RequestScheduleResponseDTO> addRequestSchedule(Long requestId, RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("AcademicRequest not found for schedule creation: " + requestId)))
            .flatMap(request -> {
                RequestSchedule schedule = modelMapper.map(requestScheduleRequestDTO, RequestSchedule.class);
                schedule.setAcademicRequestId(requestId);
                return requestScheduleRepository.save(schedule);
            })
            .map(savedSchedule -> modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class))
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error adding request schedule: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to add request schedule");
            });
    }

    /**
     * Gets schedules associated with a request.
     * @param requestId Request ID
     * @return Flux of schedules
     */
    public Flux<RequestScheduleResponseDTO> findRequestSchedulesByAcademicRequestId(Long requestId) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("AcademicRequest not found for schedule retrieval: " + requestId)))
            .flatMapMany(request -> requestScheduleRepository.findByAcademicRequestId(requestId))
            .map(requestSchedule -> modelMapper.map(requestSchedule, RequestScheduleResponseDTO.class))
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error finding request schedules: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to find request schedules");
            });
    }

    /**
     * Updates a specific schedule.
     * @param scheduleId Schedule ID
     * @param requestScheduleRequestDTO DTO with updated data
     * @return Updated schedule
     */
    public Mono<RequestScheduleResponseDTO> updateRequestSchedule(Long scheduleId, RequestScheduleRequestDTO requestScheduleRequestDTO) {
        return requestScheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new RequestScheduleNotFound("RequestSchedule not found for update: " + scheduleId)))
            .flatMap(schedule -> {
                modelMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);
                modelMapper.map(requestScheduleRequestDTO, schedule);
                return requestScheduleRepository.save(schedule);
            })
            .map(savedSchedule -> modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class))
            .onErrorMap(error -> {
                if (error instanceof RequestScheduleNotFound) {
                    return error;
                }
                log.error("Error updating request schedule: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to update request schedule");
            });
    }

    /**
     * Deletes a schedule by ID.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteRequestSchedule(Long scheduleId) {
        return requestScheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new RequestScheduleNotFound("RequestSchedule not found for deletion: " + scheduleId)))
            .flatMap(schedule -> requestScheduleRepository.deleteById(scheduleId))
            .onErrorMap(error -> {
                if (error instanceof RequestScheduleNotFound) {
                    return error;
                }
                log.error("Error deleting request schedule: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to delete request schedule");
            });
    }

    /**
     * Partially updates a schedule.
     * @param scheduleId Schedule ID
     * @param updates Map with fields to update
     * @return Updated schedule
     */
    public Mono<RequestScheduleResponseDTO> patchRequestSchedule(Long scheduleId, Map<String, Object> updates) {
        return requestScheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new RequestScheduleNotFound("RequestSchedule not found for partial update: " + scheduleId)))
            .flatMap(schedule -> {
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
                return requestScheduleRepository.save(schedule);
            })
            .map(savedSchedule -> modelMapper.map(savedSchedule, RequestScheduleResponseDTO.class))
            .onErrorMap(error -> {
                if (error instanceof RequestScheduleNotFound) {
                    return error;
                }
                log.error("Error patching request schedule: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to patch request schedule");
            });
    }

    /**
     * Processes planning requests by creating classes from combined and individual academic requests.
     * This method handles the combinations selected by the user and creates corresponding classes.
     * @param processPlanningRequestDTO DTO containing combined and individual requests
     * @return Map with processing results
     */
    public Mono<Map<String, Object>> processPlanningRequests(ProcessPlanningRequestDTO processPlanningRequestDTO) {
        Map<String, Object> result = new HashMap<>();
        List<String> createdClasses = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        Mono<Void> combinedMono = Mono.empty();
        if (processPlanningRequestDTO.getCombinedRequests() != null) {
            combinedMono = Flux.fromIterable(processPlanningRequestDTO.getCombinedRequests())
                .flatMap(combinedRequest -> {
                    String combinedObservation = createCombinedObservation(combinedRequest);
                    String className = String.join(" + ", combinedRequest.getMaterias());
                    createdClasses.add(String.format("Combinación: %s (Cupos: %d)", className, combinedRequest.getCupos()));
                    
                    return updateOriginalRequestsWithCombination(combinedRequest.getSourceIds(), combinedObservation)
                        .onErrorResume(e -> {
                            errors.add("Error procesando combinación: " + e.getMessage());
                            return Mono.empty();
                        });
                })
                .then();
        }

        Mono<Void> individualMono = Mono.empty();
        if (processPlanningRequestDTO.getIndividualRequests() != null) {
            individualMono = Flux.fromIterable(processPlanningRequestDTO.getIndividualRequests())
                .filter(individualRequest -> !"deleted".equals(individualRequest.getState()))
                .doOnNext(individualRequest -> createdClasses.add(String.format("Individual: %s - %s (Cupos: %d)", 
                        individualRequest.getProgram(), individualRequest.getMateria(), individualRequest.getCupos())))
                .onErrorResume(e -> {
                    errors.add("Error procesando solicitud individual: " + e.getMessage());
                    return Mono.empty();
                })
                .then();
        }

        return combinedMono
            .then(individualMono)
            .then(Mono.fromCallable(() -> {
                result.put("success", true);
                result.put("createdClasses", createdClasses);
                result.put("totalProcessed", createdClasses.size());
                result.put("errors", errors);
                return result;
            }))
            .onErrorResume(e -> {
                result.put("success", false);
                result.put("error", "Error general procesando solicitudes: " + e.getMessage());
                return Mono.just(result);
            });
    }

    /**
     * Creates the observation text for combined requests showing which programs were combined.
     */
    private String createCombinedObservation(CombinedRequestDTO combinedRequest) {
        StringBuilder observation = new StringBuilder("Combinación de programas: ");
        
        for (int i = 0; i < combinedRequest.getPrograms().size(); i++) {
            if (i > 0) observation.append(", ");
            observation.append(combinedRequest.getPrograms().get(i));
        }
        
        observation.append(String.format(" (Total cupos: %d)", combinedRequest.getCupos()));
        
        return observation.toString();
    }

    /**
     * Updates the original academic requests to indicate they were combined.
     */
    private Mono<Void> updateOriginalRequestsWithCombination(List<Long> sourceIds, String combinationNote) {
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(sourceIds)
            .flatMap(sourceId -> 
                academicRequestRepository.findById(sourceId)
                    .flatMap(academicRequest -> {
                        String currentObservation = academicRequest.getObservation() != null ? academicRequest.getObservation() : "";
                        academicRequest.setObservation(currentObservation + "\n" + combinationNote);
                        return academicRequestRepository.save(academicRequest);
                    })
                    .then()
                    .onErrorResume(e -> {
                        log.error("Error updating request with combination: {}", e.getMessage());
                        return Mono.empty();
                    })
            )
            .then();
    }

    /**
     * Marks an academic request as accepted (moved to planning).
     * @param requestId Request ID to mark as accepted
     * @return Mono indicating completion
     */
    public Mono<Void> markAsAccepted(Long requestId) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("Academic request not found with ID: " + requestId)))
            .flatMap(request -> {
                request.setAccepted(true);
                return academicRequestRepository.save(request);
            })
            .then()
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error marking request as accepted: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to mark request as accepted");
            });
    }

    /**
     * Marks an academic request as combined.
     * @param requestId Request ID to mark as combined
     * @return Mono indicating completion
     */
    public Mono<Void> markAsCombined(Long requestId) {
        return academicRequestRepository.findById(requestId)
            .switchIfEmpty(Mono.error(new AcademicRequestNotFound("Academic request not found with ID: " + requestId)))
            .flatMap(request -> {
                request.setCombined(true);
                return academicRequestRepository.save(request);
            })
            .then()
            .onErrorMap(error -> {
                if (error instanceof AcademicRequestNotFound) {
                    return error;
                }
                log.error("Error marking request as combined: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to mark request as combined");
            });
    }

    /**
     * Marks multiple academic requests as accepted.
     * @param requestIds List of request IDs to mark as accepted
     * @return Mono indicating completion
     */
    public Mono<Void> markMultipleAsAccepted(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return Mono.empty();
        }

        return academicRequestRepository.findAllById(requestIds)
            .doOnNext(request -> request.setAccepted(true))
            .collectList()
            .flatMap(requests -> {
                if (requests.isEmpty()) {
                    return Mono.empty();
                }
                return academicRequestRepository.saveAll(requests).then();
            })
            .onErrorMap(error -> {
                log.error("Error marking multiple requests as accepted: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to mark multiple requests as accepted");
            });
    }

    /**
     * Marks multiple academic requests as combined.
     * @param requestIds List of request IDs to mark as combined
     * @return Mono indicating completion
     */
    public Mono<Void> markMultipleAsCombined(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return Mono.empty();
        }

        return academicRequestRepository.findAllById(requestIds)
            .doOnNext(request -> request.setCombined(true))
            .collectList()
            .flatMap(requests -> {
                if (requests.isEmpty()) {
                    return Mono.empty();
                }
                return academicRequestRepository.saveAll(requests).then();
            })
            .onErrorMap(error -> {
                log.error("Error marking multiple requests as combined: {}", error.getMessage(), error);
                throw new AcademicRequestServerErrorException("Failed to mark multiple requests as combined");
            });
    }

    // ==============================================
    // Private Methods
    // ==============================================

    /**
     * Gets class schedules for a given academic request.
     * @param academicRequest The academic request
     * @return AcademicRequestResponseDTO with schedules
     */
    private Mono<AcademicRequestResponseDTO> getClassSchedulesForRequest(AcademicRequest academicRequest) {
        return requestScheduleRepository.findByAcademicRequestId(academicRequest.getId())
            .collectList()
            .flatMap(schedules -> mapToResponseDTO(academicRequest, schedules));
    }

    /**
     * Maps a saved academic request and its schedules to a response DTO.
     * @param savedRequest
     * @return AcademicRequestResponseDTO without schedules
     */
    private Mono<AcademicRequestResponseDTO> mapToResponseDTO(AcademicRequest savedRequest) {
        AcademicRequestResponseDTO responseDTO = modelMapper.map(savedRequest, AcademicRequestResponseDTO.class);

        return courseService.getCourseName(savedRequest.getCourseId())
            .flatMap(courseName ->{
                responseDTO.setCourseName(courseName);
                return this.getUserName();
            })
            .map(userName -> {
                responseDTO.setUserName(userName);
                responseDTO.setProgramName(userName);
                return responseDTO;
            });
    }

    /**
     * Obtains currently logged-in user's full name.
     * @return User's full name
     */
    private Mono<String> getUserName() {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> Mono.just(securityContext.getAuthentication().getName()))
            .flatMap(userService::getUserInformationByEmail)
            .map(userInfo -> userInfo.getName() + " " + userInfo.getLastName());
    }

    /**
     * Maps a saved academic request and its schedules to a response DTO.
     * @param savedRequest The saved academic request
     * @param schedules The associated schedules
     * @return AcademicRequestResponseDTO with schedules
     */
    private Mono<AcademicRequestResponseDTO> mapToResponseDTO(AcademicRequest savedRequest, List<RequestSchedule> schedules) {
        return mapToResponseDTO(savedRequest)
            .map(responseDTO -> {
                List<RequestScheduleResponseDTO> scheduleDTOs = schedules.stream()
                    .map(schedule -> modelMapper.map(schedule, RequestScheduleResponseDTO.class))
                    .toList();
                responseDTO.setSchedules(scheduleDTOs);
                return responseDTO;
            });
    }
    
    /**
     * Obtains the user id from current security context
     * @return Mono<Long> with user id logged-in
     */
    private Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                String userEmail = securityContext.getAuthentication().getName();
                return userService.getUserIdByEmail(userEmail);
            })
            .switchIfEmpty(Mono.error(new AcademicRequestServerErrorException("Unable to find user id by logged in user information")));
    }

    /**
     * Filters academic requests by user role and if it's the case by their section.
     * @param academicRequest the academic request to filter
     * @return Mono<Boolean> containing true if the request is under users' domain
     */
    private Mono<Boolean> filterByUserRole(AcademicRequest academicRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Authentication authentication = securityContext.getAuthentication();
                
                // Check if user has ADMIN role
                boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                if (isAdmin) {
                    // Admin can see all classes
                    return Mono.just(true);
                }

                // For ROLE_SECTION users, filter by their section
                String userEmail = authentication.getName();

                // Check if user has PROGRAM role
                boolean isProgram = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROGRAM"));

                if (isProgram) {
                    return userService.getUserIdByEmail(userEmail)
                        .flatMap(programId -> Mono.just(programId.equals(academicRequest.getUserId())));
                }

                return userService.getUserIdByEmail(userEmail)
                    .flatMap(sectionService::getSectionIdByUserId)
                    .flatMap(sectionId -> 
                        courseService.getCourseSectionId(academicRequest.getCourseId())
                    .flatMap(courseSectionId -> 
                        Mono.just(sectionId.equals(courseSectionId)))
                    )
                    .defaultIfEmpty(false);
            })
            .defaultIfEmpty(false);
    }
}