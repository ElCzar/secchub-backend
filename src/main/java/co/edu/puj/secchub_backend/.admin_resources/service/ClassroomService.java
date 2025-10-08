package co.edu.puj.secchub_backend.admin_resources.service;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassroomDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.model.Classroom;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassroomRepository;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassroomTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing classroom operations.
 * 
 * <p>This service provides comprehensive functionality for classroom management
 * including CRUD operations, search capabilities, availability checking,
 * and validation. It serves as the business logic layer for classroom
 * administration in the academic planning module.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Classroom CRUD operations with validation</li>
 * <li>Search by name, location, type, and capacity</li>
 * <li>Availability checking for scheduling</li>
 * <li>Duplicate prevention and business rule enforcement</li>
 * <li>Data transformation between entities and DTOs</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    private final ClassroomTypeRepository classroomTypeRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Get all classrooms with optional filtering.
     * 
     * @return list of all classrooms as DTOs
     */
    public List<ClassroomDTO> getAllClassrooms() {
        log.info("Obteniendo todas las aulas");
        List<Classroom> classrooms = classroomRepository.findAllWithType();
        return classrooms.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get classroom by ID.
     * 
     * @param id the classroom ID
     * @return classroom DTO
     * @throws CustomException if classroom not found
     */
    public ClassroomDTO getClassroomById(Long id) {
        log.info("Obteniendo aula por ID: {}", id);
        Classroom classroom = classroomRepository.findByIdWithType(id)
            .orElseThrow(() -> new CustomException("Aula no encontrada con ID: " + id));
        return convertToDTO(classroom);
    }
    
    /**
     * Create a new classroom.
     * 
     * @param classroomDTO the classroom data
     * @return created classroom DTO
     * @throws CustomException if validation fails
     */
    @Transactional
    public ClassroomDTO createClassroom(ClassroomDTO classroomDTO) {
        log.info("Creando nueva aula: {}", classroomDTO.getRoom());
        
        validateClassroomData(classroomDTO);
        validateNoDuplicateClassroom(classroomDTO.getRoom(), classroomDTO.getLocation(), null);
        validateClassroomType(classroomDTO.getClassroomTypeId());
        
        Classroom classroom = convertToEntity(classroomDTO);
        Classroom savedClassroom = classroomRepository.save(classroom);
        
        log.info("Aula creada exitosamente con ID: {}", savedClassroom.getId());
        return convertToDTO(savedClassroom);
    }
    
    /**
     * Update an existing classroom.
     * 
     * @param id the classroom ID
     * @param classroomDTO the updated classroom data
     * @return updated classroom DTO
     * @throws CustomException if classroom not found or validation fails
     */
    @Transactional
    public ClassroomDTO updateClassroom(Long id, ClassroomDTO classroomDTO) {
        log.info("Actualizando aula con ID: {}", id);
        
        Classroom existingClassroom = classroomRepository.findById(id)
            .orElseThrow(() -> new CustomException("Aula no encontrada con ID: " + id));
        
        validateClassroomData(classroomDTO);
        validateNoDuplicateClassroom(classroomDTO.getRoom(), classroomDTO.getLocation(), id);
        validateClassroomType(classroomDTO.getClassroomTypeId());
        
        updateClassroomFields(existingClassroom, classroomDTO);
        Classroom updatedClassroom = classroomRepository.save(existingClassroom);
        
        log.info("Aula actualizada exitosamente");
        return convertToDTO(updatedClassroom);
    }
    
    /**
     * Delete a classroom.
     * 
     * @param id the classroom ID
     * @throws CustomException if classroom not found or has dependencies
     */
    @Transactional
    public void deleteClassroom(Long id) {
        log.info("Eliminando aula con ID: {}", id);
        
        if (!classroomRepository.existsById(id)) {
            throw new CustomException("Aula no encontrada con ID: " + id);
        }
        
        // TODO: Verificar que no tenga horarios asignados antes de eliminar
        
        classroomRepository.deleteById(id);
        log.info("Aula eliminada exitosamente");
    }
    
    /**
     * Search classrooms by name.
     * 
     * @param name the room name to search for
     * @return list of matching classrooms
     */
    public List<ClassroomDTO> searchClassroomsByName(String name) {
        log.info("Buscando aulas por nombre: {}", name);
        List<Classroom> classrooms = classroomRepository.findByRoomContainingIgnoreCase(name);
        return classrooms.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get classrooms by campus.
     * 
     * @param campus the campus name
     * @return list of classrooms in the campus
     */
    public List<ClassroomDTO> getClassroomsByCampus(String campus) {
        log.info("Obteniendo aulas del campus: {}", campus);
        List<Classroom> classrooms = classroomRepository.findByCampusIgnoreCase(campus);
        return classrooms.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get classrooms by type.
     * 
     * @param typeId the classroom type ID
     * @return list of classrooms of the specified type
     */
    public List<ClassroomDTO> getClassroomsByType(Long typeId) {
        log.info("Obteniendo aulas del tipo: {}", typeId);
        List<Classroom> classrooms = classroomRepository.findByClassroomTypeId(typeId);
        return classrooms.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get available classrooms for a specific time slot.
     * 
     * @param day the day of the week
     * @param startTime the start time
     * @param endTime the end time
     * @param minCapacity optional minimum capacity
     * @return list of available classrooms
     */
    public List<ClassroomDTO> getAvailableClassrooms(String day, String startTime, String endTime, Integer minCapacity) {
        log.info("Buscando aulas disponibles para {} de {} a {}", day, startTime, endTime);
        List<Classroom> classrooms = classroomRepository.findAvailableClassrooms(day, startTime, endTime, minCapacity);
        return classrooms.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==========================================
    
    private void validateClassroomData(ClassroomDTO classroomDTO) {
        if (classroomDTO.getRoom() == null || classroomDTO.getRoom().trim().isEmpty()) {
            throw new CustomException("El nombre del aula es obligatorio");
        }
        if (classroomDTO.getLocation() == null || classroomDTO.getLocation().trim().isEmpty()) {
            throw new CustomException("La ubicación del aula es obligatoria");
        }
        if (classroomDTO.getCampus() == null || classroomDTO.getCampus().trim().isEmpty()) {
            throw new CustomException("El campus es obligatorio");
        }
        if (classroomDTO.getCapacity() == null || classroomDTO.getCapacity() <= 0) {
            throw new CustomException("La capacidad debe ser mayor a 0");
        }
        if (classroomDTO.getCapacity() > 500) {
            throw new CustomException("La capacidad no puede exceder 500 estudiantes");
        }
    }
    
    private void validateNoDuplicateClassroom(String room, String location, Long excludeId) {
        if (classroomRepository.existsByRoomAndLocationAndIdNot(room, location, excludeId)) {
            throw new CustomException("Ya existe un aula con el mismo nombre y ubicación");
        }
    }
    
    private void validateClassroomType(Long typeId) {
        if (typeId != null && !classroomTypeRepository.existsById(typeId)) {
            throw new CustomException("Tipo de aula no encontrado con ID: " + typeId);
        }
    }
    
    private ClassroomDTO convertToDTO(Classroom classroom) {
        ClassroomDTO dto = modelMapper.map(classroom, ClassroomDTO.class);
        
        // Agregar información del tipo si está disponible
        if (classroom.getClassroomType() != null) {
            dto.setTypeName(classroom.getClassroomType().getName());
        } else if (classroom.getClassroomTypeId() != null) {
            classroomTypeRepository.findById(classroom.getClassroomTypeId())
                .ifPresent(type -> dto.setTypeName(type.getName()));
        }
        
        // Establecer campos adicionales para compatibilidad con frontend
        dto.setName(classroom.getRoom());
        dto.setType(dto.getTypeName());
        dto.setHasProjector(true); // Valor por defecto, podría venir de BD
        dto.setHasAccessibility(false); // Valor por defecto, podría venir de BD
        
        // Extraer building y floor de location si es posible
        if (classroom.getLocation() != null) {
            String[] locationParts = classroom.getLocation().split(" - ");
            if (locationParts.length >= 1) {
                dto.setBuilding(locationParts[0]);
            }
            if (locationParts.length >= 2 && locationParts[1].toLowerCase().contains("piso")) {
                try {
                    String floorStr = locationParts[1].replaceAll("[^0-9]", "");
                    if (!floorStr.isEmpty()) {
                        dto.setFloor(Integer.parseInt(floorStr));
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }
        
        return dto;
    }
    
    private Classroom convertToEntity(ClassroomDTO dto) {
        return Classroom.builder()
            .classroomTypeId(dto.getClassroomTypeId())
            .campus(dto.getCampus())
            .location(dto.getLocation())
            .room(dto.getRoom())
            .capacity(dto.getCapacity())
            .build();
    }
    
    private void updateClassroomFields(Classroom classroom, ClassroomDTO dto) {
        classroom.setClassroomTypeId(dto.getClassroomTypeId());
        classroom.setCampus(dto.getCampus());
        classroom.setLocation(dto.getLocation());
        classroom.setRoom(dto.getRoom());
        classroom.setCapacity(dto.getCapacity());
    }
}
