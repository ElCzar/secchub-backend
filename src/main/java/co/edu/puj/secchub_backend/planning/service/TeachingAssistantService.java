package co.edu.puj.secchub_backend.planning.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.planning.dto.*;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.model.*;
import co.edu.puj.secchub_backend.planning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeachingAssistantService {
    private final ModelMapper modelMapper;
    private final TeachingAssistantRepository teachingAssistantRepository;
    private final TeachingAssistantScheduleRepository scheduleRepository;
    private final AdminModuleSemesterContract semesterService;

    /**
     * Creates a new teaching assistant with its associated schedules.
     * @param teachingAssistantRequestDTO with assistant data
     * @return Created teaching assistant
     */
    @Transactional
    public Mono<TeachingAssistantResponseDTO> createTeachingAssistant(TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        return Mono.fromCallable(() -> {
            TeachingAssistant teachingAssistant = modelMapper.map(teachingAssistantRequestDTO, TeachingAssistant.class);
            TeachingAssistant saved = teachingAssistantRepository.save(teachingAssistant);

            if (teachingAssistantRequestDTO.getSchedules() != null) {
                for (TeachingAssistantScheduleRequestDTO scheduleDTO : teachingAssistantRequestDTO.getSchedules()) {
                    TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
                    schedule.setDay(scheduleDTO.getDay());
                    schedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                    schedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                    schedule.setTeachingAssistantId(saved.getId());
                    
                    scheduleRepository.save(schedule);
                }
            }

            return mapToResponseDTO(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates an existing teaching assistant and its schedules.
     * @param id Teaching assistant ID
     * @param teachingAssistantRequestDTO Updated data
     * @return Updated teaching assistant
     */
    @Transactional
    public Mono<TeachingAssistantResponseDTO> updateTeachingAssistant(Long id, TeachingAssistantRequestDTO teachingAssistantRequestDTO) {
        return Mono.fromCallable(() -> {
            TeachingAssistant existing = teachingAssistantRepository.findById(id)
                    .orElseThrow(() -> new TeachingAssistantNotFoundException("TeachingAssistant not found for update: " + id));

            // Update basic fields
            existing.setClassId(teachingAssistantRequestDTO.getClassId());
            existing.setStudentApplicationId(teachingAssistantRequestDTO.getStudentApplicationId());
            existing.setWeeklyHours(teachingAssistantRequestDTO.getWeeklyHours());
            existing.setWeeks(teachingAssistantRequestDTO.getWeeks());
            existing.setTotalHours(teachingAssistantRequestDTO.getTotalHours());

            TeachingAssistant saved = teachingAssistantRepository.save(existing);

            // Update schedules - delete existing and create new ones
            scheduleRepository.deleteAll(scheduleRepository.findByTeachingAssistantId(id));
            
            if (teachingAssistantRequestDTO.getSchedules() != null) {
                for (TeachingAssistantScheduleRequestDTO scheduleDTO : teachingAssistantRequestDTO.getSchedules()) {
                    TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
                    schedule.setDay(scheduleDTO.getDay());
                    schedule.setStartTime(parseTimeString(scheduleDTO.getStartTime()));
                    schedule.setEndTime(parseTimeString(scheduleDTO.getEndTime()));
                    schedule.setTeachingAssistantId(saved.getId());
                    
                    scheduleRepository.save(schedule);
                }
            }

            return mapToResponseDTO(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a teaching assistant and all its schedules.
     * @param id Teaching assistant ID
     * @return empty Mono when done
     */
    @Transactional
    public Mono<Void> deleteTeachingAssistant(Long id) {
        return Mono.fromCallable(() -> {
            TeachingAssistant teachingAssistant = teachingAssistantRepository.findById(id)
                    .orElseThrow(() -> new TeachingAssistantNotFoundException("TeachingAssistant not found for deletion: " + id));
            
            // Delete schedules first
            scheduleRepository.deleteAll(scheduleRepository.findByTeachingAssistantId(id));
            
            // Delete teaching assistant
            teachingAssistantRepository.delete(teachingAssistant);
            
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Finds a teaching assistant by its ID.
     * @param id Teaching assistant ID
     * @return TeachingAssistantResponseDTO with the given ID
     */
    public Mono<TeachingAssistantResponseDTO> findTeachingAssistantById(Long id) {
        return Mono.fromCallable(() -> {
            TeachingAssistant teachingAssistant = teachingAssistantRepository.findById(id)
                    .orElseThrow(() -> new TeachingAssistantNotFoundException("TeachingAssistant not found for consult: " + id));
            return mapToResponseDTO(teachingAssistant);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds teaching assistants by student application ID.
     * @param studentApplicationId Student application ID
     * @return List of teaching assistants for the student application
     */
    public Mono<List<TeachingAssistantResponseDTO>> findByStudentApplicationId(Long studentApplicationId) {
        return Mono.fromCallable(() -> {
            List<TeachingAssistant> teachingAssistants = teachingAssistantRepository.findByStudentApplicationId(studentApplicationId);
            return teachingAssistants.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Obtains all teaching assistants for the current semester.
     * @return List of teaching assistants for the current semester
     */
    public Mono<List<TeachingAssistantResponseDTO>> listCurrentSemesterTeachingAssistants() {
        return Mono.fromCallable(() -> {
            Long currentSemesterId = semesterService.getCurrentSemesterId();
            List<TeachingAssistant> teachingAssistants = teachingAssistantRepository.findByCurrentSemester(currentSemesterId);
            return teachingAssistants.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Obtains all teaching assistants.
     * @return List of all teaching assistants
     */
    public Mono<List<TeachingAssistantResponseDTO>> listAllTeachingAssistants() {
        return Mono.fromCallable(() -> {
            List<TeachingAssistant> teachingAssistants = teachingAssistantRepository.findAll();
            return teachingAssistants.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates a new teaching assistant schedule.
     * @param teachingAssistantId Teaching assistant ID
     * @param scheduleRequestDTO Schedule data
     * @return Created schedule
     */
    @Transactional
    public Mono<TeachingAssistantScheduleResponseDTO> createSchedule(Long teachingAssistantId, TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            // Verify teaching assistant exists
            teachingAssistantRepository.findById(teachingAssistantId)
                    .orElseThrow(() -> new TeachingAssistantNotFoundException("TeachingAssistant not found: " + teachingAssistantId));

            TeachingAssistantSchedule schedule = new TeachingAssistantSchedule();
            schedule.setTeachingAssistantId(teachingAssistantId);
            schedule.setDay(scheduleRequestDTO.getDay());
            schedule.setStartTime(parseTimeString(scheduleRequestDTO.getStartTime()));
            schedule.setEndTime(parseTimeString(scheduleRequestDTO.getEndTime()));

            TeachingAssistantSchedule saved = scheduleRepository.save(schedule);
            return modelMapper.map(saved, TeachingAssistantScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates an existing teaching assistant schedule.
     * @param scheduleId Schedule ID
     * @param scheduleRequestDTO Updated schedule data
     * @return Updated schedule
     */
    @Transactional
    public Mono<TeachingAssistantScheduleResponseDTO> updateSchedule(Long scheduleId, TeachingAssistantScheduleRequestDTO scheduleRequestDTO) {
        return Mono.fromCallable(() -> {
            TeachingAssistantSchedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new TeachingAssistantScheduleNotFoundException("TeachingAssistantSchedule not found for update: " + scheduleId));

            schedule.setDay(scheduleRequestDTO.getDay());
            schedule.setStartTime(parseTimeString(scheduleRequestDTO.getStartTime()));
            schedule.setEndTime(parseTimeString(scheduleRequestDTO.getEndTime()));

            TeachingAssistantSchedule saved = scheduleRepository.save(schedule);
            return modelMapper.map(saved, TeachingAssistantScheduleResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a teaching assistant schedule.
     * @param scheduleId Schedule ID
     * @return empty Mono when done
     */
    @Transactional
    public Mono<Void> deleteSchedule(Long scheduleId) {
        return Mono.fromCallable(() -> {
            TeachingAssistantSchedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new TeachingAssistantScheduleNotFoundException("TeachingAssistantSchedule not found for deletion: " + scheduleId));
            
            scheduleRepository.delete(schedule);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Payroll endpoint - placeholder for future implementation.
     * @return empty Mono
     */
    public Mono<Void> generatePayroll() {
        return Mono.fromCallable(() -> {
            log.info("Payroll generation called - not yet implemented");
            // Future implementation: calculate payroll based on teaching assistant hours
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Helper method to parse time string (HH:mm:ss) to java.sql.Time
     * @param timeString Time in format "HH:mm:ss"
     * @return java.sql.Time object or null if timeString is null/empty
     * @throws IllegalArgumentException if the time string format is invalid
     */
    private Time parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            LocalTime localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
            return Time.valueOf(localTime);
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
        TeachingAssistantResponseDTO responseDTO = modelMapper.map(teachingAssistant, TeachingAssistantResponseDTO.class);
        
        // Map schedules
        List<TeachingAssistantSchedule> schedules = scheduleRepository.findByTeachingAssistantId(teachingAssistant.getId());
        List<TeachingAssistantScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> modelMapper.map(schedule, TeachingAssistantScheduleResponseDTO.class))
                .toList();
        
        responseDTO.setSchedules(scheduleDTOs);
        
        return responseDTO;
    }
}