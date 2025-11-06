package co.edu.puj.secchub_backend.planning.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.puj.secchub_backend.planning.dto.ClassroomRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassroomBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.ClassroomNotFoundException;
import co.edu.puj.secchub_backend.planning.model.Classroom;
import co.edu.puj.secchub_backend.planning.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing classroom-related operations.
 * Provides business logic for classroom CRUD operations with reactive support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ModelMapper modelMapper;

    /**
     * Gets all classrooms.
     * @return Flux containing all classroom response DTOs
     */
    public Flux<ClassroomResponseDTO> getAllClassrooms() {
        log.debug("Getting all classrooms");

        return classroomRepository.findAll()
                .map(this::mapToResponseDTO);
    }


    /**
     * Gets a classroom by its ID.
     * @param id the classroom ID
     * @return Mono containing the classroom response DTO
     * @throws ClassroomNotFoundException if classroom not found
     */
    public Mono<ClassroomResponseDTO> getClassroomById(Long id) {
        log.debug("Getting classroom by ID: {}", id);

        return classroomRepository.findById(id)
            .switchIfEmpty(Mono.error(new ClassroomNotFoundException("Classroom not found for ID: " + id)))
            .map(this::mapToResponseDTO);
    }

    /**
     * Creates a new classroom.
     * @param classroomRequestDTO the classroom request data
     * @return The created classroom response DTO
     */
    public Mono<ClassroomResponseDTO> createClassroom(ClassroomRequestDTO classroomRequestDTO) {
        return Mono.defer(() -> {
            if (classroomRequestDTO.getClassroomTypeId() == null ||
                classroomRequestDTO.getCampus() == null ||
                classroomRequestDTO.getLocation() == null ||
                classroomRequestDTO.getRoom() == null ||
                classroomRequestDTO.getCapacity() == null ||
                classroomRequestDTO.getCapacity() <= 0) {
                return Mono.error(new ClassroomBadRequestException("Invalid classroom data provided"));
            }

            log.debug("Creating new classroom: {}", classroomRequestDTO.getRoom());

            Classroom classroom = mapToEntity(classroomRequestDTO);

            return classroomRepository.save(classroom)
                    .map(this::mapToResponseDTO)
                    .doOnSuccess(saved -> log.info("Classroom created successfully: {}", saved.getRoom()))
                    .doOnError(e -> log.error("Error creating classroom: {}", e.getMessage(), e));
        });
    }

    /**
     * Updates an existing classroom.
     * @param id the classroom ID
     * @param classroomRequestDTO the updated classroom data
     * @return The updated classroom response DTO
     * @throws ClassroomNotFoundException if classroom not found
     */
    public Mono<ClassroomResponseDTO> updateClassroom(Long id, ClassroomRequestDTO classroomRequestDTO) {
        log.debug("Updating classroom with ID: {}", id);

        return classroomRepository.findById(id)
            .switchIfEmpty(Mono.error(new ClassroomNotFoundException("Classroom to update not found for ID: " + id)))
            .flatMap(existingClassroom -> {
                existingClassroom.setClassroomTypeId(classroomRequestDTO.getClassroomTypeId() != null ? classroomRequestDTO.getClassroomTypeId() : existingClassroom.getClassroomTypeId());
                existingClassroom.setCampus(classroomRequestDTO.getCampus() != null ? classroomRequestDTO.getCampus() : existingClassroom.getCampus());
                existingClassroom.setLocation(classroomRequestDTO.getLocation() != null ? classroomRequestDTO.getLocation() : existingClassroom.getLocation());
                existingClassroom.setRoom(classroomRequestDTO.getRoom() != null ? classroomRequestDTO.getRoom() : existingClassroom.getRoom());
                existingClassroom.setCapacity(classroomRequestDTO.getCapacity() != null && classroomRequestDTO.getCapacity() > 0 ? classroomRequestDTO.getCapacity() : existingClassroom.getCapacity());

                return classroomRepository.save(existingClassroom);
            })
            .map(this::mapToResponseDTO);
    }

    /**
     * Deletes a classroom.
     * @param id the classroom ID
     * @return Mono<Void> for completion
     * @throws ClassroomNotFoundException if classroom not found
     */
    @Transactional
    public Mono<Void> deleteClassroom(Long id) {
        log.debug("Deleting classroom with ID: {}", id);
        return classroomRepository.existsById(id)
            .flatMap(exists -> {
                if (Boolean.FALSE.equals(exists)) {
                    return Mono.error(new ClassroomNotFoundException("Classroom to delete not found for ID: " + id));
                }
                return classroomRepository.deleteById(id)
                    .doOnSuccess(v -> log.info("Classroom with ID: {} deleted successfully", id))
                    .doOnError(e -> log.error("Error deleting classroom with ID: {}: {}", id, e.getMessage(), e));
            });
    }

    /**
     * Gets classrooms by type.
     * @param typeId the classroom type ID
     * @return Mono containing list of classrooms of the specified type
     */
    public Mono<List<ClassroomResponseDTO>> getClassroomsByType(Long typeId) {
        log.debug("Getting classrooms by type ID: {}", typeId);
        return classroomRepository.findByClassroomTypeId(typeId)
            .map(this::mapToResponseDTO)
            .collectList();
    }

    /**
     * Maps a ClassroomRequestDTO to a Classroom entity.
     * @param classroomRequestDTO the DTO to map
     * @return the mapped Classroom entity
     */
    private Classroom mapToEntity(ClassroomRequestDTO classroomRequestDTO) {
        return modelMapper.map(classroomRequestDTO, Classroom.class);
    }

    /**
     * Maps a Classroom entity to a ClassroomResponseDTO.
     * @param classroom the entity to map
     * @return the mapped ClassroomResponseDTO
     */
    private ClassroomResponseDTO mapToResponseDTO(Classroom classroom) {
        return modelMapper.map(classroom, ClassroomResponseDTO.class);
    }
}
