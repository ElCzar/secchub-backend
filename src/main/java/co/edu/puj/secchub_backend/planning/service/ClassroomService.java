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
     * @return Mono containing list of all classroom response DTOs
     */
    public Mono<List<ClassroomResponseDTO>> getAllClassrooms() {
        log.debug("Getting all classrooms");
        return Mono.fromCallable(() -> 
            classroomRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList()
        );
    }

    /**
     * Gets a classroom by its ID.
     * @param id the classroom ID
     * @return Mono containing the classroom response DTO
     * @throws ClassroomNotFoundException if classroom not found
     */
    public Mono<ClassroomResponseDTO> getClassroomById(Long id) {
        log.debug("Getting classroom by ID: {}", id);
        return Mono.fromCallable(() -> 
            classroomRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found for ID: " + id))
        );
    }

    /**
     * Creates a new classroom.
     * @param classroomRequestDTO the classroom request data
     * @return The created classroom response DTO
     */
    @Transactional
    public ClassroomResponseDTO createClassroom(ClassroomRequestDTO classroomRequestDTO) {
        if (classroomRequestDTO.getClassroomTypeId() == null ||
            classroomRequestDTO.getCampus() == null ||
            classroomRequestDTO.getLocation() == null ||
            classroomRequestDTO.getRoom() == null ||
            classroomRequestDTO.getCapacity() == null || classroomRequestDTO.getCapacity() <= 0) {
            throw new ClassroomBadRequestException("Invalid classroom data provided");
        }
        log.debug("Creating new classroom: {}", classroomRequestDTO.getRoom());
        
        Classroom classroom = mapToEntity(classroomRequestDTO);
        Classroom savedClassroom = classroomRepository.save(classroom);
        return mapToResponseDTO(savedClassroom);
    }

    /**
     * Updates an existing classroom.
     * @param id the classroom ID
     * @param classroomRequestDTO the updated classroom data
     * @return The updated classroom response DTO
     * @throws ClassroomNotFoundException if classroom not found
     */
    @Transactional
    public ClassroomResponseDTO updateClassroom(Long id, ClassroomRequestDTO classroomRequestDTO) {
        log.debug("Updating classroom with ID: {}", id);
        Classroom existingClassroom = classroomRepository.findById(id)
            .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found for ID: " + id));
        
        // Update fields
        existingClassroom.setClassroomTypeId(classroomRequestDTO.getClassroomTypeId() != null ? classroomRequestDTO.getClassroomTypeId() : existingClassroom.getClassroomTypeId());
        existingClassroom.setCampus(classroomRequestDTO.getCampus() != null ? classroomRequestDTO.getCampus() : existingClassroom.getCampus());
        existingClassroom.setLocation(classroomRequestDTO.getLocation() != null ? classroomRequestDTO.getLocation() : existingClassroom.getLocation());
        existingClassroom.setRoom(classroomRequestDTO.getRoom() != null ? classroomRequestDTO.getRoom() : existingClassroom.getRoom());
        existingClassroom.setCapacity(classroomRequestDTO.getCapacity() != null && classroomRequestDTO.getCapacity() > 0 ? classroomRequestDTO.getCapacity() : existingClassroom.getCapacity());

        Classroom updatedClassroom = classroomRepository.save(existingClassroom);
        return mapToResponseDTO(updatedClassroom);
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
        return Mono.fromRunnable(() -> {
            if (!classroomRepository.existsById(id)) {
                throw new ClassroomNotFoundException("Classroom not found for ID: " + id);
            }
            classroomRepository.deleteById(id);
        });
    }

    /**
     * Gets classrooms by type.
     * @param typeId the classroom type ID
     * @return Mono containing list of classrooms of the specified type
     */
    public Mono<List<ClassroomResponseDTO>> getClassroomsByType(Long typeId) {
        log.debug("Getting classrooms by type ID: {}", typeId);
        return Mono.fromCallable(() -> 
            classroomRepository.findByClassroomTypeId(typeId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList()
        );
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
