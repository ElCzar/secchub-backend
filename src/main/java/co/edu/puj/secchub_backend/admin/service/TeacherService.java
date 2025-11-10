package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleTeacherContract;
import co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherUpdateRequestDTO;
import co.edu.puj.secchub_backend.admin.exception.TeacherNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Teacher;
import co.edu.puj.secchub_backend.admin.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing teacher operations.
 * Provides business logic for teacher creation, retrieval, and updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService implements AdminModuleTeacherContract{

    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;

    /**
     * Gets all teachers in the system.
     * @return List of all teachers
     */
    @Override
    public Flux<TeacherResponseDTO> getAllTeachers() {
        log.debug("Retrieving all teachers");
        return teacherRepository.findAll()
                .map(teacher -> modelMapper.map(teacher, TeacherResponseDTO.class));
    }

    /**
     * Gets a teacher by their ID.
     * @param teacherId Teacher ID
     * @return Teacher with the specified ID
     */
    @Override
    public Mono<TeacherResponseDTO> getTeacherById(Long teacherId) {
        return teacherRepository.findById(teacherId)
                .switchIfEmpty(Mono.error(new TeacherNotFoundException("Teacher not found with ID: " + teacherId)))
                .map(teacher -> modelMapper.map(teacher, TeacherResponseDTO.class));
    }

    /**
     * Creates a new teacher.
     * @param teacherCreateRequestDTO DTO with teacher creation data
     * @return Created teacher response
     */
    public Mono<TeacherResponseDTO> createTeacher(TeacherCreateRequestDTO teacherCreateRequestDTO) {
        log.debug("Creating teacher with userId: {}, employmentTypeId: {}, maxHours: {}", 
                    teacherCreateRequestDTO.getUserId(), 
                    teacherCreateRequestDTO.getEmploymentTypeId(), 
                    teacherCreateRequestDTO.getMaxHours());
        Teacher teacher = modelMapper.map(teacherCreateRequestDTO, Teacher.class);
        log.debug("Mapped to Teacher entity: {}", teacher);
        return teacherRepository.save(teacher)
                .doOnSuccess(savedTeacher -> log.debug("Successfully saved teacher: {}", savedTeacher))
                .doOnError(error -> log.error("Error saving teacher", error))
                .map(savedTeacher -> {
                    TeacherResponseDTO responseDTO = modelMapper.map(savedTeacher, TeacherResponseDTO.class);
                    log.debug("Mapped to TeacherResponseDTO: {}", responseDTO);
                    return responseDTO;
                });
    }

    /**
     * Updates a teacher's employment type and max hours.
     * @param teacherId Teacher ID
     * @param teacherUpdateRequestDTO DTO with update data
     * @return Updated teacher
     */
    public Mono<TeacherResponseDTO> updateTeacher(Long teacherId, TeacherUpdateRequestDTO teacherUpdateRequestDTO) {
        return teacherRepository.findById(teacherId)
                .switchIfEmpty(Mono.error(new TeacherNotFoundException("Teacher not found with ID: " + teacherId)))
                .flatMap(existingTeacher -> {
                    if (teacherUpdateRequestDTO.getEmploymentTypeId() != null) {
                        existingTeacher.setEmploymentTypeId(teacherUpdateRequestDTO.getEmploymentTypeId());
                    }
                    if (teacherUpdateRequestDTO.getMaxHours() != null) {
                        existingTeacher.setMaxHours(teacherUpdateRequestDTO.getMaxHours());
                    }
                    return teacherRepository.save(existingTeacher);
                })
                .map(updatedTeacher -> modelMapper.map(updatedTeacher, TeacherResponseDTO.class));
    }

    /**
     * Gets a teacher by user ID.
     * @param userId User ID
     * @return Teacher associated with the user
     */
    public Mono<TeacherResponseDTO> getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new TeacherNotFoundException("Teacher not found with User ID: " + userId)))
                .map(teacher -> modelMapper.map(teacher, TeacherResponseDTO.class));
    }

    /**
     * Gets teachers by employment type.
     * @param employmentTypeId Employment type ID
     * @return List of teachers with the specified employment type
     */
    public Flux<TeacherResponseDTO> getTeachersByEmploymentType(Long employmentTypeId) {
        log.debug("Retrieving teachers with employment type ID: {}", employmentTypeId);
        return teacherRepository.findByEmploymentTypeId(employmentTypeId)
                .map(teacher -> modelMapper.map(teacher, TeacherResponseDTO.class));
    }

    /**
     * Gets teachers with minimum available hours.
     * @param minHours Minimum hours required
     * @return List of teachers with adequate hours
     */
    public Flux<TeacherResponseDTO> getTeachersWithMinHours(Integer minHours) {
        log.debug("Retrieving teachers with minimum {} hours", minHours);
        return teacherRepository.findByMaxHoursGreaterThanEqual(minHours)
                .map(teacher -> modelMapper.map(teacher, TeacherResponseDTO.class));
    }

    /**
     * Implements the AdminModuleTeacherContract to get teacher ID by user ID.
     * @param userId User ID
     * @return Teacher ID
     */
    @Override
    public Mono<Long> getTeacherIdByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new TeacherNotFoundException("Teacher not found with User ID: " + userId)))
                .map(Teacher::getId);
    }
}