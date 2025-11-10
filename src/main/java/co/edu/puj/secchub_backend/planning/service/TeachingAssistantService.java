package co.edu.puj.secchub_backend.planning.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.contract.IntegrationModuleStudentApplicationContract;
import co.edu.puj.secchub_backend.planning.dto.*;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantServerErrorException;
import co.edu.puj.secchub_backend.planning.model.*;
import co.edu.puj.secchub_backend.planning.repository.*;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeachingAssistantService {
    private final TeachingAssistantRepository teachingAssistantRepository;
    private final TeachingAssistantScheduleRepository scheduleRepository;

    private final ModelMapper modelMapper;
    
    private final SecurityModuleUserContract userService;
    private final AdminModuleSectionContract sectionService;
    private final AdminModuleSemesterContract semesterService;
    private final IntegrationModuleStudentApplicationContract studentApplicationService;
    
    private final TransactionalOperator transactionalOperator;

    /**
     * Creates a new teaching assistant with its associated schedules.
     * @param teachingAssistantRequestDTO with assistant data
     * @return Created teaching assistant
     */
    public Mono<TeachingAssistantResponseDTO> createTeachingAssistant(TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        TeachingAssistant teachingAssistant = modelMapper.map(teachingAssistantRequestDTO, TeachingAssistant.class);

        return teachingAssistantRepository.save(teachingAssistant)
            .flatMap(savedTeachingAssistant -> {
                TeachingAssistantResponseDTO responseDTO = mapToResponseDTO(savedTeachingAssistant);
                List<TeachingAssistantScheduleRequestDTO> schedulesDTO = teachingAssistantRequestDTO.getSchedules();
                
                if (schedulesDTO == null || schedulesDTO.isEmpty()) {
                    return Mono.just(responseDTO);
                }
                
                return Flux.fromIterable(schedulesDTO)
                    .map(scheduleDTO -> {
                        TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
                        schedule.setDay(scheduleDTO.getDay());
                        schedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                        schedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                        schedule.setTeachingAssistantId(savedTeachingAssistant.getId());
                        return schedule;
                    })
                    .collectList()
                    .flatMap(list ->
                        scheduleRepository.saveAll(Flux.fromIterable(list))
                            .map(this::mapToScheduleResponseDTO)
                            .collectList()
                            .map(schedules -> {
                                responseDTO.setSchedules(schedules);
                                return responseDTO;
                            })
                    );
            })
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                log.error("Error creating TeachingAssistant: {}", error.getMessage(), error);
                return new TeachingAssistantBadRequestException("Failed to create TeachingAssistant: " + error.getMessage());
            });
    }

    /**
     * Updates an existing teaching assistant and its schedules.
     * @param id Teaching assistant ID
     * @param teachingAssistantRequestDTO Updated data
     * @return Updated teaching assistant
     */
    public Mono<TeachingAssistantResponseDTO> updateTeachingAssistant(Long id, TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        return teachingAssistantRepository.findById(id)
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for update: " + id)))
            .flatMap(existingTeachingAssistant -> {
                // Configure ModelMapper to skip null values
                ModelMapper updateMapper = new ModelMapper();
                updateMapper.getConfiguration()
                    .setPropertyCondition(context -> context.getSource() != null);
                
                // Map non-null values from DTO to existing entity
                updateMapper.map(teachingAssistantRequestDTO, existingTeachingAssistant);
                
                return teachingAssistantRepository.save(existingTeachingAssistant);
            })
            .flatMap(updatedTeachingAssistant -> {
                TeachingAssistantResponseDTO responseDTO = mapToResponseDTO(updatedTeachingAssistant);
                List<TeachingAssistantScheduleRequestDTO> schedulesDTO = teachingAssistantRequestDTO.getSchedules();
                
                if (schedulesDTO == null || schedulesDTO.isEmpty()) {
                    return Mono.just(responseDTO);
                }
                
                return scheduleRepository.deleteAll(scheduleRepository.findByTeachingAssistantId(id))
                    .then(
                        Flux.fromIterable(schedulesDTO)
                            .map(scheduleDTO -> {
                                TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
                                schedule.setDay(scheduleDTO.getDay());
                                schedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                                schedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                                schedule.setTeachingAssistantId(updatedTeachingAssistant.getId());
                                return schedule;
                            })
                            .collectList()
                            .flatMap(list ->
                                scheduleRepository.saveAll(Flux.fromIterable(list))
                                    .map(this::mapToScheduleResponseDTO)
                                    .collectList()
                                    .map(schedules -> {
                                        responseDTO.setSchedules(schedules);
                                        return responseDTO;
                                    })
                            )
                    );
            })
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                log.error("Error updating TeachingAssistant: {}", error.getMessage(), error);
                if (error instanceof TeachingAssistantNotFoundException) {
                    return error;
                }
                return new TeachingAssistantServerErrorException("Failed to update TeachingAssistant: " + error.getMessage());
            });
    }

    /**
     * Deletes a teaching assistant and all its schedules.
     * @param id Teaching assistant ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteTeachingAssistant(Long id) {
        return teachingAssistantRepository.findById(id)
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for deletion: " + id)))
            .flatMap(teachingAssistant -> 
                scheduleRepository.deleteAll(scheduleRepository.findByTeachingAssistantId(id))
                    .then(teachingAssistantRepository.delete(teachingAssistant))
            )
            .as(transactionalOperator::transactional)
            .onErrorMap(error -> {
                log.error("Error deleting TeachingAssistant: {}", error.getMessage(), error);
                if (error instanceof TeachingAssistantNotFoundException) {
                    return error;
                }
                return new TeachingAssistantServerErrorException("Failed to delete TeachingAssistant: " + error.getMessage());
            });
    }

    /**
     * Finds a teaching assistant by its ID.
     * @param id Teaching assistant ID
     * @return TeachingAssistantResponseDTO with the given ID
     */
    public Mono<TeachingAssistantResponseDTO> findTeachingAssistantById(Long id) {
        return teachingAssistantRepository.findById(id)
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found: " + id)))
            .map(this::mapToResponseDTO)
            .flatMap(responseDTO -> 
                scheduleRepository.findByTeachingAssistantId(id)
                    .map(this::mapToScheduleResponseDTO)
                    .collectList()
                    .map(schedules -> {
                        responseDTO.setSchedules(schedules);
                        return responseDTO;
                    })
            );
    }

    /**
     * Finds teaching assistants by student application ID.
     * @param studentApplicationId Student application ID
     * @return List of teaching assistants for the student application
     */
    public Mono<TeachingAssistantResponseDTO> findByStudentApplicationId(Long studentApplicationId) {
        return teachingAssistantRepository.findByStudentApplicationId(studentApplicationId)
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for StudentApplicationId: " + studentApplicationId)))
            .map(this::mapToResponseDTO)
            .flatMap(responseDTO -> 
                scheduleRepository.findByTeachingAssistantId(responseDTO.getId())
                    .map(this::mapToScheduleResponseDTO)
                    .collectList()
                    .map(schedules -> {
                        responseDTO.setSchedules(schedules);
                        return responseDTO;
                    })
            );
    }

    /**
     * Obtains all teaching assistants.
     * @return List of all teaching assistants
     */
    public Flux<TeachingAssistantResponseDTO> listAllTeachingAssistants() {
        return teachingAssistantRepository.findAll()
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .map(this::mapToResponseDTO);
    }

    /**
     * Creates a new teaching assistant schedule.
     * @param teachingAssistantId Teaching assistant ID
     * @param scheduleRequestDTO Schedule data
     * @return Created schedule
     */
    public Mono<TeachingAssistantScheduleResponseDTO> createSchedule(Long teachingAssistantId, TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return teachingAssistantRepository.findById(teachingAssistantId)
            .filterWhen(this::filterTeachingAssistantsByUserSection)
            .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for schedule creation: " + teachingAssistantId)))
            .flatMap(teachingAssistant -> {
                TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
                schedule.setDay(scheduleRequestDTO.getDay());
                schedule.setStartTime(parseTimeString(scheduleRequestDTO.getStartTime()));
                schedule.setEndTime(parseTimeString(scheduleRequestDTO.getEndTime()));
                schedule.setTeachingAssistantId(teachingAssistantId);
                
                return scheduleRepository.save(schedule)
                    .map(this::mapToScheduleResponseDTO);
            })
            .as(transactionalOperator::transactional);
    }

    /**
     * Updates an existing teaching assistant schedule.
     * @param scheduleId Schedule ID
     * @param scheduleRequestDTO Updated schedule data
     * @return Updated schedule
     */
    public Mono<TeachingAssistantScheduleResponseDTO> updateSchedule(Long scheduleId, TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return scheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new TeachingAssistantScheduleNotFoundException("TeachingAssistantSchedule not found for update: " + scheduleId)))
            .flatMap(schedule -> 
                teachingAssistantRepository.findById(schedule.getTeachingAssistantId())
                    .filterWhen(this::filterTeachingAssistantsByUserSection)
                    .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for schedule update: " + schedule.getTeachingAssistantId())))
                    .thenReturn(schedule)
            )
            .flatMap(existingSchedule -> {
                existingSchedule.setDay(scheduleRequestDTO.getDay());
                existingSchedule.setStartTime(parseTimeString(scheduleRequestDTO.getStartTime()));
                existingSchedule.setEndTime(parseTimeString(scheduleRequestDTO.getEndTime()));
                
                return scheduleRepository.save(existingSchedule)
                    .map(this::mapToScheduleResponseDTO);
            })
            .as(transactionalOperator::transactional);
    }

    /**
     * Deletes a teaching assistant schedule.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    public Mono<Void> deleteSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
            .switchIfEmpty(Mono.error(new TeachingAssistantScheduleNotFoundException("TeachingAssistantSchedule not found for deletion: " + scheduleId)))
            .flatMap(schedule -> 
                teachingAssistantRepository.findById(schedule.getTeachingAssistantId())
                    .filterWhen(this::filterTeachingAssistantsByUserSection)
                    .switchIfEmpty(Mono.error(new TeachingAssistantNotFoundException("TeachingAssistant not found for schedule deletion: " + schedule.getTeachingAssistantId())))
                    .thenReturn(schedule)
            )
            .flatMap(scheduleRepository::delete)
            .as(transactionalOperator::transactional);
    }

    /**
     * Obtains schedule conflicts for teaching assistants in the current semester.
     * Groups overlapping schedules by teaching assistants and filters based on user permissions.
     * Creates separate conflict groups for each cluster of overlapping schedules.
     * @return Flux of teaching assistant schedule conflicts
     */
    public Mono<List<TeachingAssistantScheduleConflictResponseDTO>> getTeachingAssistantScheduleConflicts() {
        return semesterService.getCurrentSemesterId()
            .flatMap(currentSemesterId ->
                scheduleRepository.findConflictingSchedulesWithDetailsBySemesterId(currentSemesterId)
                .collectList()
                .flatMap(schedulesWithConflicts ->
                    Flux.fromIterable(schedulesWithConflicts)
                    .collectMultimap(TeachingAssistantScheduleWithDetailsDTO::getUserId)
                    .flatMapMany(groupedByUser ->
                        Flux.fromIterable(groupedByUser.entrySet())
                        .filter(entry -> entry.getValue().size() >= 2)
                        .flatMap(entry -> {
                            Long userId = entry.getKey();
                            List<TeachingAssistantScheduleWithDetailsDTO> userSchedules = 
                                (List<TeachingAssistantScheduleWithDetailsDTO>) entry.getValue();
                            
                            // Group schedules into clusters based on overlaps
                            List<List<TeachingAssistantScheduleWithDetailsDTO>> clusters = 
                                groupTASchedulesIntoOverlapClusters(userSchedules);
                            
                            // Create a conflict DTO for each cluster with at least 2 schedules
                            return Flux.fromIterable(clusters)
                                .filter(cluster -> cluster.size() >= 2)
                                // Filter based on user section permissions - keep cluster if at least one TA belongs to user's section
                                .filterWhen(cluster -> 
                                    Flux.fromIterable(cluster)
                                        .flatMap(schedule -> {
                                            // Convert DTO to TeachingAssistant model for filtering
                                            TeachingAssistant ta = new TeachingAssistant();
                                            ta.setId(schedule.getTeachingAssistantId());
                                            ta.setStudentApplicationId(schedule.getStudentApplicationId());
                                            return filterTeachingAssistantsByUserSection(ta);
                                        })
                                        .any(isAllowed -> isAllowed)
                                )
                                .flatMap(cluster -> {
                                    TeachingAssistantScheduleWithDetailsDTO firstSchedule = cluster.get(0);

                                    // Get user details
                                    return userService.getUserInformationById(userId)
                                        .map(userInfo -> {
                                            TeachingAssistantScheduleConflictResponseDTO conflictDTO = 
                                                new TeachingAssistantScheduleConflictResponseDTO();
                                            conflictDTO.setUserId(userId);
                                            conflictDTO.setUserName(userInfo.getName());
                                            conflictDTO.setConflictTeachingAssistants(
                                                cluster.stream()
                                                    .map(TeachingAssistantScheduleWithDetailsDTO::getTeachingAssistantId)
                                                    .distinct()
                                                    .toList()
                                            );
                                            conflictDTO.setDay(firstSchedule.getDay());
                                            conflictDTO.setConflictStartTime(
                                                cluster.stream()
                                                    .map(TeachingAssistantScheduleWithDetailsDTO::getStartTime)
                                                    .min(LocalTime::compareTo)
                                                    .orElse(firstSchedule.getStartTime())
                                            );
                                            conflictDTO.setConflictEndTime(
                                                cluster.stream()
                                                    .map(TeachingAssistantScheduleWithDetailsDTO::getEndTime)
                                                    .max(LocalTime::compareTo)
                                                    .orElse(firstSchedule.getEndTime())
                                            );
                                            return conflictDTO;
                                        })
                                        .onErrorResume(e -> {
                                            log.warn("Error getting user details for userId {}: {}", 
                                                userId, e.getMessage());
                                            return Mono.empty();
                                        });
                                });
                        })
                    )
                    .collectList()
                )
            )
            .onErrorMap(e -> {
                log.error("Error retrieving teaching assistant schedule conflicts: {}", e.getMessage());
                throw new TeachingAssistantServerErrorException(
                    "Error retrieving teaching assistant schedule conflicts: " + e.getMessage());
            });
    }

    // ========================================================================
    // Private Methods
    // ========================================================================

    /**
     * Groups TA schedules into clusters where schedules that overlap are in the same cluster.
     * Uses a greedy algorithm to detect overlaps between schedules.
     * @param schedules List of TA schedules to group
     * @return List of clusters, where each cluster contains overlapping schedules
     */
    private List<List<TeachingAssistantScheduleWithDetailsDTO>> groupTASchedulesIntoOverlapClusters(
        List<TeachingAssistantScheduleWithDetailsDTO> schedules
    ) {
        if (schedules.isEmpty()) {
            return List.of();
        }
        
        List<List<TeachingAssistantScheduleWithDetailsDTO>> clusters = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (int i = 0; i < schedules.size()*schedules.size(); i++) {
            int index = i % schedules.size();
            List<TeachingAssistantScheduleWithDetailsDTO> cluster = new ArrayList<>();
            cluster.add(schedules.get(index));
            
            for (int j = 0; j < schedules.size(); j++) {
                if (
                    j == index || 
                    processed.contains(schedules.get(j).getScheduleId() + "-" + schedules.get(index).getScheduleId()) ||
                    processed.contains(schedules.get(index).getScheduleId() + "-" + schedules.get(j).getScheduleId())
                ) {
                    continue;
                }
                
                final int currentIndex = j;
                boolean overlapsWithAll = cluster.stream()
                    .allMatch(s -> taSchedulesOverlap(s, schedules.get(currentIndex)));
                
                if (overlapsWithAll) {
                    final int finalJ = j;
                    cluster.stream()
                    .forEach(s ->
                        processed.add(s.getScheduleId() + "-" + schedules.get(finalJ).getScheduleId())
                    );
                    cluster.add(schedules.get(j));
                }
            }
            
            if (cluster.size() > 1) {
                clusters.add(cluster);
            }
        }
        return clusters;
    }
    
    /**
     * Checks if two TA schedules overlap in time on the same day.
     * @param schedule1 First schedule
     * @param schedule2 Second schedule
     * @return true if schedules overlap, false otherwise
     */
    private boolean taSchedulesOverlap(
        TeachingAssistantScheduleWithDetailsDTO schedule1, 
        TeachingAssistantScheduleWithDetailsDTO schedule2
    ) {
        // Must be on the same day
        if (!schedule1.getDay().equals(schedule2.getDay())) {
            return false;
        }
        
        // Check time overlap: (start1 < end2) AND (end1 > start2)
        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) && 
            schedule1.getEndTime().isAfter(schedule2.getStartTime());
    }

    /**
     * Helper method to parse time string (HH:mm:ss) to java.sql.Time
     * @param timeString Time in format "HH:mm:ss"
     * @return java.sql.Time object or null if timeString is null/empty
     * @throws IllegalArgumentException if the time string format is invalid
     */
    private LocalTime parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: '" + timeString + "'. Expected format: HH:mm:ss", e);
        }
    }

    /**
     * Helper method to convert TeachingAssistant entity to ResponseDTO.
     * @param teachingAssistant TeachingAssistant entity
     * @return TeachingAssistantResponseDTO
     */
    private TeachingAssistantResponseDTO mapToResponseDTO(TeachingAssistant teachingAssistant) {
        return modelMapper.map(teachingAssistant, TeachingAssistantResponseDTO.class);
        
    }

    /**
     * Helper method to convert TeachingAssistantSchedule entity to ResponseDTO.
     * @param schedule TeachingAssistantSchedule entity
     * @return TeachingAssistantScheduleResponseDTO
     */
    private TeachingAssistantScheduleResponseDTO mapToScheduleResponseDTO(TeachingAssistantSchedule schedule) {
        return modelMapper.map(schedule, TeachingAssistantScheduleResponseDTO.class);
    }

    /**
     * Filters teaching assistants by user section.
     * @param teachingAssistant TeachingAssistant entity
     * @return Mono<Boolean> indicating if the teaching assistant belongs to the user's section
     */
    private Mono<Boolean> filterTeachingAssistantsByUserSection(TeachingAssistant teachingAssistant) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Authentication authentication = securityContext.getAuthentication();
                
                // Check if user has ADMIN role
                boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                if (isAdmin) {
                    return Mono.just(true);
                }
                
                // For ROLE_SECTION users, filter by their section
                String userEmail = authentication.getName();
                
                return userService.getUserIdByEmail(userEmail)
                    .flatMap(sectionService::getSectionIdByUserId)
                    .flatMap(userSectionId -> 
                        studentApplicationService.isApplicationOfSection(
                            teachingAssistant.getStudentApplicationId(), 
                            userSectionId
                        )
                    )
                    .defaultIfEmpty(false);
            })
            .defaultIfEmpty(false);
    }
}