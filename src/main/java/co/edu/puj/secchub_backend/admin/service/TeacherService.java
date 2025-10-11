package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.TeacherCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherUpdateRequestDTO;
import co.edu.puj.secchub_backend.admin.exception.TeacherNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Teacher;
import co.edu.puj.secchub_backend.admin.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Service for managing teacher operations.
 * Provides business logic for teacher creation, retrieval, and updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;

    /**
     * Gets all teachers in the system.
     * @return List of all teachers
     */
    public List<TeacherResponseDTO> getAllTeachers() {
        log.debug("Retrieving all teachers");
        return teacherRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets a teacher by their ID.
     * @param teacherId Teacher ID
     * @return Teacher with the specified ID
     */
    public Mono<TeacherResponseDTO> getTeacherById(Long teacherId) {
        return Mono.fromCallable(() -> {
            log.debug("Retrieving teacher with ID: {}", teacherId);
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with ID: " + teacherId));
            return mapToResponseDTO(teacher);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates a new teacher.
     * @param teacherCreateRequestDTO DTO with teacher creation data
     * @return Created teacher response
     */
    @Transactional
    public TeacherResponseDTO createTeacher(TeacherCreateRequestDTO teacherCreateRequestDTO) {
        log.debug("Creating new teacher for user ID: {}", teacherCreateRequestDTO.getUserId());
        
        if (teacherRepository.existsByUserId(teacherCreateRequestDTO.getUserId())) {
            throw new IllegalArgumentException("Teacher already exists for user ID: " + teacherCreateRequestDTO.getUserId());
        }

        Teacher teacher = modelMapper.map(teacherCreateRequestDTO, Teacher.class);
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        log.info("Successfully created teacher with ID: {} for user ID: {}", 
                savedTeacher.getId(), savedTeacher.getUserId());

        return modelMapper.map(savedTeacher, TeacherResponseDTO.class);
    }

    /**
     * Updates a teacher's employment type and max hours.
     * @param teacherId Teacher ID
     * @param teacherUpdateRequestDTO DTO with update data
     * @return Updated teacher
     */
    @Transactional
    public Mono<TeacherResponseDTO> updateTeacher(Long teacherId, TeacherUpdateRequestDTO teacherUpdateRequestDTO) {
        return Mono.fromCallable(() -> {
            log.debug("Updating teacher with ID: {}", teacherId);
            
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with ID: " + teacherId));

            // Update only the allowed fields
            if (teacherUpdateRequestDTO.getEmploymentTypeId() != null) {
                teacher.setEmploymentTypeId(teacherUpdateRequestDTO.getEmploymentTypeId());
            }
            
            if (teacherUpdateRequestDTO.getMaxHours() != null) {
                teacher.setMaxHours(teacherUpdateRequestDTO.getMaxHours());
            }

            Teacher savedTeacher = teacherRepository.save(teacher);
            
            log.info("Successfully updated teacher with ID: {}", savedTeacher.getId());
            
            return mapToResponseDTO(savedTeacher);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a teacher by user ID.
     * @param userId User ID
     * @return Teacher associated with the user
     */
    public Mono<TeacherResponseDTO> getTeacherByUserId(Long userId) {
        return Mono.fromCallable(() -> {
            log.debug("Retrieving teacher for user ID: {}", userId);
            Teacher teacher = teacherRepository.findByUserId(userId)
                    .orElseThrow(() -> new TeacherNotFoundException("Teacher not found for user ID: " + userId));
            return mapToResponseDTO(teacher);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets teachers by employment type.
     * @param employmentTypeId Employment type ID
     * @return List of teachers with the specified employment type
     */
    public List<TeacherResponseDTO> getTeachersByEmploymentType(Long employmentTypeId) {
        log.debug("Retrieving teachers with employment type ID: {}", employmentTypeId);
        return teacherRepository.findByEmploymentTypeId(employmentTypeId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Gets teachers with minimum available hours.
     * @param minHours Minimum hours required
     * @return List of teachers with adequate hours
     */
    public List<TeacherResponseDTO> getTeachersWithMinHours(Integer minHours) {
        log.debug("Retrieving teachers with minimum {} hours", minHours);
        return teacherRepository.findByMaxHoursGreaterThanEqual(minHours).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Helper method to map Teacher entity to TeacherResponseDTO.
     */
    private TeacherResponseDTO mapToResponseDTO(Teacher teacher) {
        return modelMapper.map(teacher, TeacherResponseDTO.class);
    }
}